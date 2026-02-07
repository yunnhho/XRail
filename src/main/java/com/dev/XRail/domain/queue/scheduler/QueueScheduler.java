package com.dev.XRail.domain.queue.scheduler;

import com.dev.XRail.domain.queue.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final WaitingQueueService waitingQueueService;
    private final StringRedisTemplate redisTemplate;

    private static final String ACTIVE_KEY = "queue:active";
    // 1초마다 실행
    // 트래픽 상황에 따라 '100'이라는 숫자를 동적으로 조절하는 것이 NetFunnel의 핵심 기술입니다.
    // 여기서는 고정값(100TPS)으로 가정합니다.
    @Scheduled(fixedDelay = 1000)
    public void scheduleActivation() {
        long allowCount = 100L; // 초당 100명 진입 허용
        waitingQueueService.activateUsers(allowCount);
    }

    // 2. [New] 좀비 토큰 청소 (1분 주기)
    /*
    @Scheduled(fixedRate = 60000)
    public void cleanupZombieTokens() {
        // 구현 필요: 별도의 ZSet(active_timestamps)을 두어 score가 오래된 유저를 active set에서 제거
    }
    */
}