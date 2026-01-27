package com.dev.XRail.domain.train.controller;

import com.dev.XRail.common.dto.ApiResponse;
import com.dev.XRail.domain.train.dto.SeatResponse;
import com.dev.XRail.domain.train.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // GET /api/schedules/1/seats?startStationId=1&endStationId=3
    @GetMapping("/{scheduleId}/seats")
    public ApiResponse<List<SeatResponse>> getSeats(
            @PathVariable Long scheduleId,
            @RequestParam Long startStationId,
            @RequestParam Long endStationId
    ) {
        List<SeatResponse> seats = seatService.getSeatStatus(scheduleId, startStationId, endStationId);
        return ApiResponse.success(seats);
    }
}