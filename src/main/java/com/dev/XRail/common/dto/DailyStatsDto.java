package com.dev.XRail.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsDto {
    private Long totalRevenue;
    private Long totalTickets;
}