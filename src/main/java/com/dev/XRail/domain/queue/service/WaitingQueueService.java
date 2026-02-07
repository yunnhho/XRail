package com.dev.XRail.domain.queue.service;

import com.dev.XRail.domain.queue.dto.QueueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitingQueueService {

    private final StringRedisTemplate redisTemplate;

    private static final String WAITING_KEY = "queue:waiting";
    private static final String ACTIVE_KEY = "queue:active";
    private static final long ESTIMATED_PROCESS_TIME = 10;

    // 1. 대기열 등록
    public void registerQueue(Long userId) {
        String member = userId.toString();
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ACTIVE_KEY, member))) {
            return;
        }
        redisTemplate.opsForZSet().addIfAbsent(WAITING_KEY, member, System.currentTimeMillis());
    }

    // 2. 상태 조회 (Fix: Race Condition 방어)
    public QueueResponse getQueueStatus(Long userId) {
        String member = userId.toString();

        // 2-1. 이미 입장 가능한지 1차 확인
        if (isActive(member)) {
            return activeResponse();
        }

        // 2-2. 대기열 순번 확인
        Long rank = redisTemplate.opsForZSet().rank(WAITING_KEY, member);

        if (rank == null) {
            // 대기열에 없으면 등록 시도
            registerQueue(userId);

            // 등록 후 다시 조회
            rank = redisTemplate.opsForZSet().rank(WAITING_KEY, member);

            // [CRITICAL FIX] 등록 직후 스케줄러가 가져가서 Active가 된 경우, rank는 또 null이 됨
            if (rank == null) {
                // Active 상태인지 재확인 (여기서 true면 스케줄러가 데려간 것)
                if (isActive(member)) {
                    return activeResponse();
                }

                // Active도 아니고 Waiting도 아니면? (거의 없겠지만 방어 코드)
                // 잠시 후 다시 시도하도록 유도
                return QueueResponse.builder()
                        .status("WAITING")
                        .rank(-1L) // 순번 확인 중
                        .expectedWaitSeconds(1L)
                        .build();
            }
        }

        // 2-3. 대기 정보 반환
        return QueueResponse.builder()
                .status("WAITING")
                .rank(rank + 1) // 0등 -> 1등
                .expectedWaitSeconds(rank * ESTIMATED_PROCESS_TIME / 1000)
                .build();
    }

    // 3. 스케줄러용
    public void activateUsers(long count) {
        Set<String> users = redisTemplate.opsForZSet().range(WAITING_KEY, 0, count - 1);
        if (users == null || users.isEmpty()) return;

        redisTemplate.opsForSet().add(ACTIVE_KEY, users.toArray(new String[0]));
        redisTemplate.opsForZSet().remove(WAITING_KEY, users.toArray(new Object[0]));
        log.info("Activated {} users", users.size());
    }

    // 4. Interceptor용 가벼운 조회
    public boolean isAllowedByToken(Long userId) {
        return isActive(userId.toString());
    }

    public void removeUser(Long userId) {
        redisTemplate.opsForSet().remove(ACTIVE_KEY, userId.toString());
    }

    // Helper
    private boolean isActive(String member) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ACTIVE_KEY, member));
    }

    private QueueResponse activeResponse() {
        return QueueResponse.builder()
                .status("ACTIVE")
                .rank(0L)
                .expectedWaitSeconds(0L)
                .build();
    }
}