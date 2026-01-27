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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final ScheduleRepository scheduleRepository;
    private final TicketRepository ticketRepository;
    private final RouteStationRepository routeStationRepository;

    /**
     * 예약 가능한 좌석 조회
     */
    public List<SeatResponse> getSeatStatus(Long scheduleId, Long startStationId, Long endStationId) {
        // 1. 스케줄 및 열차 정보 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄입니다."));

        Long routeId = schedule.getRoute().getId();

        // 2. 출발역/도착역의 순서(Index) 조회 (구간 계산용)
        Integer startIdx = routeStationRepository.findSequenceByRouteIdAndStationId(routeId, startStationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노선에 존재하지 않는 출발역입니다."));
        Integer endIdx = routeStationRepository.findSequenceByRouteIdAndStationId(routeId, endStationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노선에 존재하지 않는 도착역입니다."));

        if (startIdx >= endIdx) {
            throw new IllegalArgumentException("출발역은 도착역보다 앞서야 합니다.");
        }

        // 3. 해당 스케줄의 해당 구간에 이미 예매된 티켓 조회 (Sold Out List)
        List<Ticket> soldTickets = ticketRepository.findOverlappingTickets(scheduleId, startIdx, endIdx);

        // 빠른 조회를 위해 예매된 Seat ID를 Set으로 변환
        Set<Long> soldSeatIds = soldTickets.stream()
                .map(ticket -> ticket.getSeat().getId())
                .collect(Collectors.toSet());

        // 4. 열차의 모든 물리 좌석을 순회하며 상태 매핑
        List<SeatResponse> response = new ArrayList<>();

        // Lazy Loading 방지를 위해 Fetch Join 된 쿼리를 쓰거나, Transactional 안에서 접근
        for (Carriage carriage : schedule.getTrain().getCarriages()) {
            for (Seat seat : carriage.getSeats()) {
                boolean isReserved = soldSeatIds.contains(seat.getId());

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