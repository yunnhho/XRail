package com.dev.XRail.domain.schedule.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.dev.XRail.domain.schedule.entity.Schedule;

public interface ScheduleRepositoryCustom {
    List<Schedule> findSchedules(Long departureStationId, Long arrivalStationId, LocalDate date, LocalTime startTime);
}
