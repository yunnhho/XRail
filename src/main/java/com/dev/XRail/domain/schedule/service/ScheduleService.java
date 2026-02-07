package com.dev.XRail.domain.schedule.service;

import com.dev.XRail.domain.reservation.repository.TicketRepository;
import com.dev.XRail.domain.schedule.dto.ScheduleResponse;
import com.dev.XRail.domain.schedule.entity.Schedule;
import com.dev.XRail.domain.schedule.repository.ScheduleRepository;
import com.dev.XRail.domain.station.entity.RouteStation;
import com.dev.XRail.domain.station.repository.RouteStationRepository;
import com.dev.XRail.domain.train.entity.Carriage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RouteStationRepository routeStationRepository;
    private final TicketRepository ticketRepository;

    public List<ScheduleResponse> searchSchedules(Long departureStationId, Long arrivalStationId, LocalDate date, LocalTime time) {
        List<Schedule> schedules = scheduleRepository.findSchedules(departureStationId, arrivalStationId, date, time);

        return schedules.stream()
                .map(schedule -> {
                    ScheduleResponse response = ScheduleResponse.from(schedule);
                    
                    // 요금 계산 로직
                    Long routeId = schedule.getRoute().getId();
                    Optional<RouteStation> startRs = routeStationRepository.findByRouteIdAndStationId(routeId, departureStationId);
                    Optional<RouteStation> endRs = routeStationRepository.findByRouteIdAndStationId(routeId, arrivalStationId);

                    if (startRs.isPresent() && endRs.isPresent()) {
                        double distance = Math.abs(endRs.get().getCumulativeDistance() - startRs.get().getCumulativeDistance());
                        long price = (long) (distance * 125) + 5000; // km당 125원 + 기본요금 5000원
                        // 100원 단위 절삭
                        price = (price / 100) * 100;
                        response.setPrice(price);
                        
                        // 도착 시간 보정 (출발시간 + 소요시간)
                        // 시속 200km 가정
                        int minutes = (int) (distance / 200.0 * 60.0);
                        response.setArrivalTime(response.getDepartureTime().plusMinutes(minutes));

                        // [Sold Out Check]
                        // 1. 전체 좌석 수 계산 (Train -> Carriage -> sum(seatCount))
                        // *주의* N+1 발생 가능성 (Carriage 조회) - 현재 규모에선 허용
                        int totalSeats = schedule.getTrain().getCarriages().stream()
                                .mapToInt(Carriage::getSeatCount)
                                .sum();
                        
                        // 2. 현재 구간에 팔린 좌석 수 조회
                        // startRs.get().getStationSequence(), endRs.get().getStationSequence()
                        // 구간 방향(상행/하행)에 따라 start/end 대소관계가 다를 수 있으나, 
                        // RouteStationRepositoryImpl에서 이미 '출발역 < 도착역' (상행/하행 구분된 Route 기준) 필터링함.
                        // 따라서 min/max 처리 또는 그대로 사용.
                        int startIdx = startRs.get().getStationSequence();
                        int endIdx = endRs.get().getStationSequence();
                        
                        long soldCount = ticketRepository.countOverlappingTickets(
                                schedule.getId(), 
                                Math.min(startIdx, endIdx), 
                                Math.max(startIdx, endIdx)
                        );
                        
                        if (soldCount >= totalSeats) {
                            response.setSoldOut(true);
                        }
                    }
                    
                    return response;
                })
                .collect(Collectors.toList());
    }
}
