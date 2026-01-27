package com.dev.XRail.domain.schedule.service;

import com.dev.XRail.domain.schedule.dto.ScheduleResponse;
import com.dev.XRail.domain.schedule.entity.Schedule;
import com.dev.XRail.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    /**
     * 스케줄 검색
     * @param routeId 노선 ID (예: 1=경부선)
     * @param date 출발 날짜
     * @param time 출발 시각 (이 시간 이후의 열차 검색)
     */
    public List<ScheduleResponse> searchSchedules(Long routeId, LocalDate date, LocalTime time) {
        List<Schedule> schedules = scheduleRepository.findSchedules(routeId, date, time);

        return schedules.stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }
}