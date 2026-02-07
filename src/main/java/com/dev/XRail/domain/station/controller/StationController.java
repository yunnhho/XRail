package com.dev.XRail.domain.station.controller;

import com.dev.XRail.common.dto.ApiResponse;
import com.dev.XRail.domain.station.entity.Station;
import com.dev.XRail.domain.station.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationRepository stationRepository;

    @GetMapping
    public ApiResponse<List<Station>> getStations() {
        return ApiResponse.success(stationRepository.findAll());
    }
}
