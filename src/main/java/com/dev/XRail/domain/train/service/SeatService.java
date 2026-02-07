package com.dev.XRail.domain.train.service;

import com.dev.XRail.domain.reservation.entity.Ticket;
import com.dev.XRail.domain.reservation.repository.TicketRepository;
import com.dev.XRail.domain.schedule.entity.Schedule;
import com.dev.XRail.domain.schedule.repository.ScheduleRepository;
import com.dev.XRail.domain.station.repository.RouteStationRepository;
import com.dev.XRail.domain.train.dto.SeatResponse;
import com.dev.XRail.domain.train.entity.Carriage;
import com.dev.XRail.domain.train.entity.Seat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 추가
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final ScheduleRepository scheduleRepository;
    private final TicketRepository ticketRepository;
    private final RouteStationRepository routeStationRepository;
    private final StringRedisTemplate redisTemplate;

    /**
     * 예약 가능한 좌석 조회
     */
    public List<SeatResponse> getSeatStatus(Long scheduleId, Long startStationId, Long endStationId) {
        // 1. 스케줄 및 열차 정보 조회
        Schedule schedule = scheduleRepository.findWithTrainById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄입니다."));

        // [Logic] 출발 5분 이내 열차는 예약 불가 처리
        LocalDateTime departureDateTime = LocalDateTime.of(schedule.getDepartureDate(), schedule.getDepartureTime());
        if (departureDateTime.isBefore(LocalDateTime.now().plusMinutes(5))) {
            throw new com.dev.XRail.common.exception.BusinessException("LATE_RESERVATION", "출발 5분 전에는 예약이 불가능합니다.");
        }

        Long routeId = schedule.getRoute().getId();

        // 2. 출발역/도착역의 순서 조회
        Integer startIdx = routeStationRepository.findSequenceByRouteIdAndStationId(routeId, startStationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노선에 존재하지 않는 출발역입니다."));
        Integer endIdx = routeStationRepository.findSequenceByRouteIdAndStationId(routeId, endStationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노선에 존재하지 않는 도착역입니다."));

        log.debug("[getSeatStatus] Schedule: {}, Route: {}, Segment: {} -> {}", scheduleId, routeId, startIdx, endIdx);

        // 현재 요청 구간의 비트마스크 생성
        long reqMask = 0;
        for (int i = startIdx; i < endIdx; i++) reqMask |= (1L << i);

        // 3. DB 예매 내역 조회
        List<Ticket> soldTickets = ticketRepository.findOverlappingTickets(scheduleId, startIdx, endIdx);
        Set<Long> soldSeatIds = soldTickets.stream()
                .map(ticket -> ticket.getSeat().getId())
                .collect(Collectors.toSet());
        
        log.debug("[getSeatStatus] DB Sold Seat IDs: {}", soldSeatIds);

        // 4. 좌석 상태 매핑 (DB + Redis)
        List<SeatResponse> response = new ArrayList<>();

        for (Carriage carriage : schedule.getTrain().getCarriages()) {
            for (Seat seat : carriage.getSeats()) {
                // DB 체크
                boolean isReserved = soldSeatIds.contains(seat.getId());

                // Redis 실시간 체크 (결제 중인 좌석 등)
                if (!isReserved) {
                    String key = "sch:" + scheduleId + ":seat:" + seat.getId();
                    String currentMaskStr = redisTemplate.opsForValue().get(key);
                    if (currentMaskStr != null) {
                        long currentMask = Long.parseLong(currentMaskStr);
                        if ((currentMask & reqMask) != 0) {
                            isReserved = true;
                            log.debug("[getSeatStatus] Redis Reserved Seat ID: {}", seat.getId());
                        }
                    }
                }

                response.add(SeatResponse.builder()
                        .seatId(seat.getId())
                        .carriageNumber(carriage.getCarriageNumber())
                        .seatNumber(seat.getSeatNumber())
                        .isReserved(isReserved)
                        .build());
            }
        }

        return response;
    }
}