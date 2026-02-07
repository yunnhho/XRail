package com.dev.XRail.domain.schedule.controller;

import com.dev.XRail.common.dto.ApiResponse;
import com.dev.XRail.domain.schedule.dto.ScheduleResponse;
import com.dev.XRail.domain.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ApiResponse<List<ScheduleResponse>> getSchedules(
            @RequestParam Long departureStationId,
            @RequestParam Long arrivalStationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time
    ) {
        if (time == null) time = LocalTime.MIN;

        List<ScheduleResponse> result = scheduleService.searchSchedules(departureStationId, arrivalStationId, date, time);
        return ApiResponse.success(result);
    }
}
