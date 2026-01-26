package com.dev.XRail.domain.queue.scheduler;

import com.dev.XRail.domain.queue.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final WaitingQueueService waitingQueueService;

    // 1초마다 실행
    // 트래픽 상황에 따라 '100'이라는 숫자를 동적으로 조절하는 것이 NetFunnel의 핵심 기술입니다.
    // 여기서는 고정값(100TPS)으로 가정합니다.
    @Scheduled(fixedDelay = 1000)
    public void enterUser() {
        long allowCount = 100L; // 초당 100명 진입 허용
        waitingQueueService.activateUsers(allowCount);
    }
}