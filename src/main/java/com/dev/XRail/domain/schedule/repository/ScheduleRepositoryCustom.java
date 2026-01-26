package com.dev.XRail.domain.schedule.repository;

import com.dev.XRail.domain.schedule.entity.Schedule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleRepositoryCustom {
    // 특정 노선, 날짜, 시간 이후의 스케줄 조회
    List<Schedule> findSchedules(Long routeId, LocalDate date, LocalTime startTime);
}