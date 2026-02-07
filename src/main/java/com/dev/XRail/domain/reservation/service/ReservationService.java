package com.dev.XRail.domain.reservation.service;

import com.dev.XRail.common.exception.BusinessException;
import com.dev.XRail.domain.reservation.dto.ReservationDetailResponse;
import com.dev.XRail.domain.reservation.dto.ReservationRequest;
import com.dev.XRail.domain.reservation.entity.Reservation;
import com.dev.XRail.domain.reservation.entity.ReservationStatus;
import com.dev.XRail.domain.reservation.entity.Ticket;
import com.dev.XRail.domain.reservation.repository.ReservationRepository;
import com.dev.XRail.domain.reservation.repository.TicketRepository;
import com.dev.XRail.domain.schedule.entity.Schedule;
import com.dev.XRail.domain.schedule.repository.ScheduleRepository;
import com.dev.XRail.domain.station.entity.Station;
import com.dev.XRail.domain.station.repository.RouteStationRepository;
import com.dev.XRail.domain.station.repository.StationRepository;
import com.dev.XRail.domain.train.entity.Seat;
import com.dev.XRail.domain.train.repository.SeatRepository;
import com.dev.XRail.domain.user.entity.NonMember;
import com.dev.XRail.domain.user.entity.User;
import com.dev.XRail.domain.user.repository.NonMemberRepository;
import com.dev.XRail.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final ScheduleRepository scheduleRepository;
    private final StationRepository stationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final NonMemberRepository nonMemberRepository;
    private final RedisService redisService;
    private final RouteStationRepository routeStationRepository;

    @Transactional
    public Long createReservation(Long userId, ReservationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new BusinessException("SCHEDULE_NOT_FOUND", "스케줄을 찾을 수 없습니다."));

        // [Logic] 출발 5분 이내 열차는 예약 불가 처리 (보안 및 데이터 정합성)
        LocalDateTime departureDateTime = LocalDateTime.of(schedule.getDepartureDate(), schedule.getDepartureTime());
        if (departureDateTime.isBefore(LocalDateTime.now().plusMinutes(5))) {
            throw new BusinessException("LATE_RESERVATION", "출발 5분 전에는 예약이 불가능합니다.");
        }

        // [Logic] 프론트엔드 인덱스 대신 백엔드 노선 시퀀스를 직접 조회하여 정확성 보장
        Long routeId = schedule.getRoute().getId();
        Integer startIdx = routeStationRepository.findSequenceByRouteIdAndStationId(routeId, request.getStartStationId())
                .orElseThrow(() -> new BusinessException("STATION_NOT_IN_ROUTE", "출발역이 노선에 없습니다."));
        Integer endIdx = routeStationRepository.findSequenceByRouteIdAndStationId(routeId, request.getEndStationId())
                .orElseThrow(() -> new BusinessException("STATION_NOT_IN_ROUTE", "도착역이 노선에 없습니다."));

        List<Ticket> tickets = new ArrayList<>();
        List<Long> acquiredSeatIds = new ArrayList<>(); // 롤백용

        try {
            for (Long seatId : request.getSeatIds()) {
                // 1. Redis Bitmasking 선점 (Atomic) - 내부적으로 startIdx, endIdx 사용
                boolean isAcquired = redisService.acquireSeat(
                        schedule.getId(), seatId, startIdx, endIdx);
                
                if (!isAcquired) {
                    throw new BusinessException("SEAT_ALREADY_TAKEN", "이미 예약된 좌석이 포함되어 있습니다.");
                }
                acquiredSeatIds.add(seatId);

                // 2. DB 레벨 2차 검증 (보수적 접근)
                Seat seat = seatRepository.findByIdWithLock(seatId).orElseThrow();
                boolean isOccupied = ticketRepository.existsOverlap(
                        schedule.getId(), seatId, startIdx, endIdx);
                
                if (isOccupied) {
                    throw new BusinessException("SEAT_ALREADY_TAKEN", "이미 예약된 좌석입니다: " + seat.getSeatNumber());
                }

                Ticket ticket = Ticket.builder()
                        .schedule(schedule)
                        .seat(seat)
                        .startStation(stationRepository.findById(request.getStartStationId()).orElseThrow())
                        .endStation(stationRepository.findById(request.getEndStationId()).orElseThrow())
                        .startStationIdx(startIdx)
                        .endStationIdx(endIdx)
                        .price(request.getPrice() / request.getSeatIds().size())
                        .build();
                tickets.add(ticket);
            }

            Reservation reservation = Reservation.builder()
                    .user(user)
                    .status(ReservationStatus.PENDING)
                    .totalPrice(request.getPrice())
                    .reservedAt(LocalDateTime.now())
                    .build();
            
            for (Ticket t : tickets) {
                t.setReservation(reservation);
                reservation.getTickets().add(t);
            }
            
            return reservationRepository.save(reservation).getId();

        } catch (Exception e) {
            // [Rollback] DB 저장 실패 시 Redis 선점 해제
            for (Long acquiredSeatId : acquiredSeatIds) {
                redisService.releaseSeat(schedule.getId(), acquiredSeatId, startIdx, endIdx);
            }
            throw e;
        }
    }

    @Transactional
    public String payReservation(Long userId, Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("RESERVATION_NOT_FOUND", "예약 정보를 찾을 수 없습니다."));
        
        if (!res.getUser().getId().equals(userId)) {
            throw new BusinessException("FORBIDDEN", "권한이 없습니다.");
        }
        
        res.updateStatus(ReservationStatus.PAID);
        
        // [Fix] 프록시 객체 대응: NonMemberRepository를 사용하여 명시적 조회
        if (res.getUser().getRole() == com.dev.XRail.domain.user.entity.UserRole.NON_MEMBER) {
            return nonMemberRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "비회원 정보를 찾을 수 없습니다."))
                    .getAccessCode();
        }
        return null;
    }

    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId).orElseThrow();
        if (!res.getUser().getId().equals(userId)) throw new BusinessException("FORBIDDEN", "권한이 없습니다.");
        res.updateStatus(ReservationStatus.CANCELLED);
    }

    public org.springframework.data.domain.Page<ReservationDetailResponse> getMyReservations(Long userId, org.springframework.data.domain.Pageable pageable) {
        return reservationRepository.findAllByUserId(userId, pageable)
                .map(ReservationDetailResponse::from);
    }
}
