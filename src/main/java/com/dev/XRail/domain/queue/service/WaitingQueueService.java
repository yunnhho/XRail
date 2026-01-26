package com.dev.XRail.domain.queue.service;

import com.dev.XRail.domain.queue.dto.QueueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitingQueueService {

    private final StringRedisTemplate redisTemplate;

    private static final String WAITING_KEY = "queue:waiting"; // 대기열 (ZSet)
    private static final String ACTIVE_KEY = "queue:active";   // 입장 가능 (Set)
    private static final long ESTIMATED_PROCESS_TIME = 10;     // 1명당 처리 예상 시간 (ms, 단순 계산용)

    // 1. 대기열 등록 (이미 등록된 경우 무시)
    public void registerQueue(Long userId) {
        String member = userId.toString();
        // Active 상태면 재등록 불필요
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ACTIVE_KEY, member))) {
            return;
        }
        // Waiting 상태면 스코어 유지, 없으면 현재 시간으로 추가
        redisTemplate.opsForZSet().addIfAbsent(WAITING_KEY, member, System.currentTimeMillis());
    }

    // 2. 현재 상태 조회 (Polling)
    public QueueResponse getQueueStatus(Long userId) {
        String member = userId.toString();

        // 2-1. 입장 가능 상태인지 확인
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ACTIVE_KEY, member))) {
            return QueueResponse.builder()
                    .status("ACTIVE")
                    .rank(0L)
                    .expectedWaitSeconds(0L)
                    .build();
        }

        // 2-2. 대기열 순번 확인
        Long rank = redisTemplate.opsForZSet().rank(WAITING_KEY, member);
        if (rank == null) {
            // 대기열에도 없으면 등록 유도 (혹은 예외)
            registerQueue(userId);
            rank = redisTemplate.opsForZSet().rank(WAITING_KEY, member);
        }

        return QueueResponse.builder()
                .status("WAITING")
                .rank(rank + 1) // 0부터 시작하므로 +1
                .expectedWaitSeconds(rank * ESTIMATED_PROCESS_TIME / 1000) // 단순 계산
                .build();
    }

    // 3. (Scheduler용) 대기열 -> 활성열 이동
    public void activateUsers(long count) {
        // ZSet에서 가장 오래된 N명 조회
        Set<String> users = redisTemplate.opsForZSet().range(WAITING_KEY, 0, count - 1);

        if (users == null || users.isEmpty()) {
            return;
        }

        // Active Set에 추가하고 Waiting ZSet에서 제거
        // (Lua Script로 원자성 보장하면 더 좋음)
        redisTemplate.opsForSet().add(ACTIVE_KEY, users.toArray(new String[0]));
        redisTemplate.opsForZSet().remove(WAITING_KEY, users.toArray(new Object[0]));

        log.info("Activated {} users", users.size());
    }

    // 4. 작업 완료 후 토큰 만료 (예매 끝난 유저)
    public void removeUser(Long userId) {
        redisTemplate.opsForSet().remove(ACTIVE_KEY, userId.toString());
    }
}