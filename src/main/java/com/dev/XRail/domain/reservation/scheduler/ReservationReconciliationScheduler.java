package com.dev.XRail.domain.reservation.scheduler;

import com.dev.XRail.domain.reservation.entity.Ticket;
import com.dev.XRail.domain.reservation.repository.TicketRepository;
import com.dev.XRail.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationReconciliationScheduler {

    private final StringRedisTemplate redisTemplate;
    private final TicketRepository ticketRepository;
    private final ScheduleRepository scheduleRepository;

    // [Spec] 5분 주기 실행 (유령 좌석 해제)
    @Scheduled(fixedDelay = 300000) // 5분
    public void reconcileSeats() {
        log.info("[Reconciliation] Start checking consistency between Redis and DB...");

        long totalScanCount = 0;
        long totalFixCount = 0;

        // 1. 현재 예매 가능한(미래) 스케줄 ID 목록만 DB에서 조회 (Bulk Select)
        List<Long> activeScheduleIds = scheduleRepository.findActiveScheduleIds(LocalDate.now());
        log.info("[Reconciliation] Active Schedules: {}", activeScheduleIds.size());

        for (Long scheduleId : activeScheduleIds) {
            totalFixCount += processSchedule(scheduleId);
        }

        log.info("[Reconciliation] Finished. Total Fixed: {}", totalFixCount);
    }

    private long processSchedule(Long scheduleId) {
        long fixCount = 0;

        // 2. 해당 스케줄의 모든 유효 티켓 조회 (Bulk Select) -> Memory Map
        List<Ticket> tickets = ticketRepository.findAllActiveTicketsByScheduleId(scheduleId);
        Map<Long, Long> dbMaskMap = calculateMasks(tickets);

        // 3. 해당 스케줄의 Redis Key만 스캔 (sch:{scheduleId}:seat:*)
        ScanOptions options = ScanOptions.scanOptions().match("sch:" + scheduleId + ":seat:*").count(100).build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String redisKey = cursor.next();
                
                try {
                    // Key Parsing: sch:{id}:seat:{seatId}
                    String[] parts = redisKey.split(":");
                    Long seatId = Long.parseLong(parts[3]);

                    String redisValue = redisTemplate.opsForValue().get(redisKey);
                    long redisMask = (redisValue == null) ? 0L : Long.parseLong(redisValue);

                    // DB 상태 (Map에서 O(1) 조회)
                    long dbMask = dbMaskMap.getOrDefault(seatId, 0L);

                    // 불일치 발견 시 보정
                    if (redisMask != dbMask) {
                        log.warn("[Mismatch Detected] Key: {}, Redis: {}, DB: {} -> Fixing...",
                                redisKey, Long.toBinaryString(redisMask), Long.toBinaryString(dbMask));

                        if (dbMask == 0) {
                            redisTemplate.delete(redisKey);
                        } else {
                            redisTemplate.opsForValue().set(redisKey, String.valueOf(dbMask));
                        }
                        fixCount++;
                    }

                    // DB에는 있는데 Redis에는 없는 경우는?
                    // -> 이 로직은 Redis Key를 기준으로 돌기 때문에, 'DB에는 있고 Redis에 없는' 경우는
                    //    Redis에 해당 Key가 아예 없으므로 커버되지 않음.
                    //    하지만 '예매'는 Redis가 먼저 생성되므로, Redis에 없고 DB에만 있는 경우는
                    //    'Redis TTL 만료' 외에는 드뭄.
                    //    필요하다면 dbMaskMap의 KeySet을 순회하며 Redis에 없는 것을 채워넣는 로직 추가 가능.
                    //    (현재는 '유령 좌석 해제'가 주 목적이므로 Pass)
                    
                    // DB Map에서 확인된 Seat는 제거 (Optional: 양방향 검증 시 사용)
                    // dbMaskMap.remove(seatId); 

                } catch (Exception e) {
                    log.error("[Reconciliation Error] Key: {}, Msg: {}", redisKey, e.getMessage());
                }
            }
        }
        return fixCount;
    }

    private Map<Long, Long> calculateMasks(List<Ticket> tickets) {
        Map<Long, Long> map = new HashMap<>();

        for (Ticket ticket : tickets) {
            Long seatId = ticket.getSeat().getId();
            long mask = map.getOrDefault(seatId, 0L);

            for (int i = ticket.getStartStationIdx(); i < ticket.getEndStationIdx(); i++) {
                mask |= (1L << i);
            }
            map.put(seatId, mask);
        }
        return map;
    }
}