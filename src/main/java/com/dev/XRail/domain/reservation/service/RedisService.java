package com.dev.XRail.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // Lua Script: 비트마스크 연산 (조회 + 점유)
    // KEYS[1]: 스케줄+좌석 키
    // ARGV[1]: 요청한 구간 마스크 (예: 0110)
    // ARGV[2]: TTL (초)
    private static final String SCRIPT_RESERVE =
            "local current = redis.call('GET', KEYS[1]) " +
                    "if (current == false) then current = 0 else current = tonumber(current) end " +
                    "local mask = tonumber(ARGV[1]) " +
                    "if (bit.band(current, mask) == 0) then " +  // 겹치는 비트가 없으면
                    "    local nextState = bit.bor(current, mask) " + // 비트 점유 (OR 연산)
                    "    redis.call('SET', KEYS[1], nextState) " +
                    "    redis.call('EXPIRE', KEYS[1], ARGV[2]) " + // TTL 갱신 (예: 5분)
                    "    return 1 " + // 성공
                    "else " +
                    "    return 0 " + // 실패 (이미 예약됨)
                    "end";

    // Lua Script: 롤백 (보상 트랜잭션용)
    // 점유했던 비트만 다시 0으로 끔 (XOR or AND NOT)
    private static final String SCRIPT_ROLLBACK =
            "local current = redis.call('GET', KEYS[1]) " +
                    "if (current == false) then return 0 end " +
                    "local mask = tonumber(ARGV[1]) " +
                    "local nextState = bit.bxor(current, mask) " + // XOR로 해당 비트만 반전 (1->0)
                    // 주의: XOR은 기존이 0이었으면 1이 되므로, 정확히는 (current & ~mask)가 안전함.
                    // 여기서는 단순화를 위해, 자신이 점유했던 mask를 그대로 들고 온다고 가정.
                    "redis.call('SET', KEYS[1], nextState) " +
                    "return 1";

    /**
     * 좌석 점유 시도 (Atomic)
     * @param scheduleId 스케줄 ID
     * @param seatId 좌석 ID
     * @param startIdx 구간 시작 인덱스
     * @param endIdx 구간 종료 인덱스 (실제 점유는 endIdx - 1 까지)
     * @return 성공 시 true
     */
    public boolean acquireSeat(Long scheduleId, Long seatId, int startIdx, int endIdx) {
        String key = "sch:" + scheduleId + ":seat:" + seatId;
        long mask = createBitMask(startIdx, endIdx);

        // Lua Script 실행
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(SCRIPT_RESERVE, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), String.valueOf(mask), "300"); // 300초(5분) 임시 점유

        return result != null && result == 1;
    }

    /**
     * 좌석 점유 해제 (보상 트랜잭션)
     */
    public void releaseSeat(Long scheduleId, Long seatId, int startIdx, int endIdx) {
        String key = "sch:" + scheduleId + ":seat:" + seatId;
        long mask = createBitMask(startIdx, endIdx);

        // 현재 상태에서 해당 마스크 부분만 제거 (AND NOT 연산 구현이 복잡하므로 간단히 값을 덮어쓰거나 감소시키는 방식 고려)
        // 안전하게: bit.band(current, bit.bnot(mask)) 사용 권장.
        String script =
                "local current = redis.call('GET', KEYS[1]) " +
                        "if (current == false) then return 0 end " +
                        "local mask = tonumber(ARGV[1]) " +
                        "local nextState = bit.band(tonumber(current), bit.bnot(mask)) " +
                        "redis.call('SET', KEYS[1], nextState) " +
                        "return 1";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        redisTemplate.execute(redisScript, Collections.singletonList(key), String.valueOf(mask));
        log.info("[Redis Rollback] Key: {}, Mask: {}", key, mask);
    }

    /**
     * 구간 인덱스를 비트마스크로 변환
     * 예: start=0, end=2 (0~1구간) -> 2^0 + 2^1 = 3 (이진수 11)
     */
    private long createBitMask(int startIdx, int endIdx) {
        long mask = 0;
        // endIdx는 포함하지 않음 (Off-by-One 방지)
        for (int i = startIdx; i < endIdx; i++) {
            mask |= (1L << i);
        }
        return mask;
    }
}