package com.dev.XRail.domain.reservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ReservationRequest {
    @NotNull
    private Long scheduleId;

    @NotEmpty
    private List<Long> seatIds;

    @NotNull
    private Long startStationId;

    @NotNull
    private Long endStationId;

    private Integer startStationIdx;
    private Integer endStationIdx;
    
    private Long price;
}
