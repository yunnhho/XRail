package com.dev.XRail.domain.queue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QueueResponse {
    private String status; // "WAITING", "ACTIVE"
    private Long rank;     // 대기 순번 (내 앞에 몇 명 있는지)
    private Long expectedWaitSeconds; // 예상 대기 시간
}