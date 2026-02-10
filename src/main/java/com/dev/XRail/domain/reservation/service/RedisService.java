package com.dev.XRail.domain.reservation.service;

import jakarta.annotation.Resource; // @Resource 사용을 위해 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // [Refactor] 외부 파일로 분리된 스크립트 Bean 주입
    // RedisScriptConfig에서 등록한 Bean 이름과 일치해야 함
    @Resource(name = "reservationScript")
    private RedisScript<Long> reservationScript;

    @Resource(name = "rollbackScript")
    private RedisScript<Long> rollbackScript;

    private static final String SEAT_RESERVATION_TTL = "300"; // 5분

    /**
     * 좌석 점유 시도 (Atomic)
     * @return 성공 시 true
     */
    public boolean acquireSeat(Long scheduleId, Long seatId, int startIdx, int endIdx) {
        String key = "sch:" + scheduleId + ":seat:" + seatId;
        // 기존 로직 유지: Java에서 마스크 생성 -> Lua로 전달
        long mask = createBitMask(startIdx, endIdx);

        // Lua Script 실행
        Long result = redisTemplate.execute(
                reservationScript,
                Collections.singletonList(key), // KEYS[1]
                String.valueOf(mask),           // ARGV[1]
                SEAT_RESERVATION_TTL            // ARGV[2]: TTL
        );

        return result != null && result == 1L;
    }

    /**
     * 좌석 점유 해제 (보상 트랜잭션)
     */
    public void releaseSeat(Long scheduleId, Long seatId, int startIdx, int endIdx) {
        String key = "sch:" + scheduleId + ":seat:" + seatId;
        long mask = createBitMask(startIdx, endIdx);

        try {
            redisTemplate.execute(
                    rollbackScript,
                    Collections.singletonList(key),
                    String.valueOf(mask)
            );
            log.info("[Redis Rollback] Key: {}, Mask: {}", key, mask);
        } catch (Exception e) {
            log.error("[Redis Rollback Failed] Key: {}, Error: {}", key, e.getMessage());
        }
    }

    /**
     * 구간 인덱스를 비트마스크로 변환 (기존 코드 유지)
     * 예: start=0, end=2 (0~1구간) -> 2^0 + 2^1 = 3 (이진수 11)
     */
    private long createBitMask(int startIdx, int endIdx) {
        if (endIdx > 32) {
            throw new IllegalArgumentException("현재 시스템은 최대 32개 구간까지만 지원합니다.");
        }

        long mask = 0;
        // endIdx는 포함하지 않음 (Off-by-One 방지)
        for (int i = startIdx; i < endIdx; i++) {
            mask |= (1L << i);
        }
        return mask;
    }
}