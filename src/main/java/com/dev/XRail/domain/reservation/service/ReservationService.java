package com.dev.XRail.domain.reservation.service;

import com.dev.XRail.common.exception.BusinessException; // 예외 클래스 필요 (추후 생성)
import com.dev.XRail.domain.reservation.dto.ReservationEvent;
import com.dev.XRail.domain.reservation.dto.ReservationRequest;
import com.dev.XRail.domain.reservation.entity.Reservation;
import com.dev.XRail.domain.reservation.entity.ReservationStatus;
import com.dev.XRail.domain.reservation.entity.Ticket;
import com.dev.XRail.domain.reservation.repository.ReservationRepository;
import com.dev.XRail.domain.reservation.repository.TicketRepository;
import com.dev.XRail.domain.schedule.entity.Schedule;
import com.dev.XRail.domain.schedule.repository.ScheduleRepository;
import com.dev.XRail.domain.station.entity.Station;
import com.dev.XRail.domain.station.repository.StationRepository;
import com.dev.XRail.domain.train.entity.Seat;
import com.dev.XRail.domain.train.repository.SeatRepository;
import com.dev.XRail.domain.user.entity.User;
import com.dev.XRail.domain.user.repository.UserRepository;
import com.dev.XRail.infra.kafka.ReservationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final RedisService redisService;
    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final StationRepository stationRepository;
    private final ReservationProducer reservationProducer;

    /**
     * [핵심] 예매 요청 처리
     * 1. Redis 비트마스킹으로 구간 선점 (Lock)
     * 2. RDB 트랜잭션 진입
     * 3. 티켓 생성 및 저장
     * 4. 실패 시 Redis 롤백 (보상 트랜잭션)
     */
    public Long createReservation(Long userId, ReservationRequest request) {
        // 1. 데이터 조회 (캐싱 적용 가능하나 일단 DB 조회)
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new RuntimeException("스케줄을 찾을 수 없습니다."));

        // 2. Redis 구간 선점 (Atomic) - 트랜잭션 밖에서 수행 (성능)
        boolean acquired = redisService.acquireSeat(
                schedule.getId(),
                request.getSeatId(),
                request.getStartStationIdx(),
                request.getEndStationIdx()
        );

        if (!acquired) {
            throw new RuntimeException("이미 선택된 좌석입니다. (Redis Lock)");
        }

        try {
            // 3. DB 트랜잭션 저장
            Long reservationId = saveReservationInTransaction(userId, request, schedule);
            // 3. [New] Kafka 이벤트 발행 (비동기)
            ReservationEvent event = ReservationEvent.builder()
                    .reservationId(reservationId)
                    .userId(userId)
                    .scheduleId(schedule.getId())
                    .seatId(request.getSeatId())
                    .trainNumber(schedule.getTrain().getTrainNumber()) // Lazy Loading 주의
                    .departureDate(schedule.getDepartureDate().toString())
                    .seatNumber("Unknown") // Seat 정보 조회 필요
                    .eventCreatedAt(LocalDateTime.now())
                    .build();

            reservationProducer.sendReservationComplete(event);

            return reservationId;
        } catch (Exception e) {
            // [Critical] DB 저장 실패 시 Redis 점유 해제 (보상 트랜잭션)
            log.error("예매 DB 저장 실패. Redis 롤백 수행. error={}", e.getMessage());
            redisService.releaseSeat(
                    schedule.getId(),
                    request.getSeatId(),
                    request.getStartStationIdx(),
                    request.getEndStationIdx()
            );
            throw e;
        }
    }

    @Transactional // 여기서부터 RDB 트랜잭션
    public Long saveReservationInTransaction(Long userId, ReservationRequest request, Schedule schedule) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new RuntimeException("좌석 없음"));

        Station startStation = stationRepository.findById(request.getStartStationId())
                .orElseThrow(() -> new RuntimeException("출발역 없음"));

        Station endStation = stationRepository.findById(request.getEndStationId())
                .orElseThrow(() -> new RuntimeException("도착역 없음"));

        // [Double Check] DB 레벨 중복 체크 (TicketRepository의 Overlap Query)
        boolean existsOverlap = ticketRepository.existsOverlap(
                schedule.getId(),
                seat.getId(),
                request.getStartStationIdx(),
                request.getEndStationIdx()
        );

        if (existsOverlap) {
            throw new RuntimeException("이미 예약된 좌석입니다. (DB Check)");
        }

        // 예매 헤더 생성
        Reservation reservation = Reservation.builder()
                .user(user)
                .status(ReservationStatus.PENDING) // 결제 대기
                .totalPrice(request.getPrice()) // 실제론 요금 계산 로직 필요
                .reservedAt(LocalDateTime.now())
                .build();

        reservationRepository.save(reservation);

        // 티켓 상세 생성
        Ticket ticket = Ticket.builder()
                .reservation(reservation)
                .schedule(schedule)
                .seat(seat)
                .startStation(startStation)
                .endStation(endStation)
                .startStationIdx(request.getStartStationIdx())
                .endStationIdx(request.getEndStationIdx())
                .price(request.getPrice())
                .build();

        ticketRepository.save(ticket);

        return reservation.getId();
    }
}