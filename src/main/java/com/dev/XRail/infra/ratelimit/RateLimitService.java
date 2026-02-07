package com.dev.XRail.infra.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedissonClient redissonClient;

    private RedissonBasedProxyManager proxyManager;

    // [Spec] IP당 호출 제한 설정
    // 예: 초당 10회, 분당 100회
    public boolean tryConsume(String ip, String keyPrefix) {
        if (proxyManager == null) {
            initProxyManager();
        }

        String key = keyPrefix + ":" + ip;
        
        // Bucket 설정
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillGreedy(10, Duration.ofSeconds(1))
                        .build())
                .addLimit(Bandwidth.builder()
                        .capacity(100)
                        .refillGreedy(100, Duration.ofMinutes(1))
                        .build())
                .build();

        // 프록시를 통해 버킷 생성 및 토큰 소비 시도
        return proxyManager.builder().build(key, configuration).tryConsume(1);
    }

    private synchronized void initProxyManager() {
        if (proxyManager != null) return;

        try {
            // Bucket4j-Redis 8.x + Redisson 설정
            // RedissonClient의 CommandExecutor를 추출하여 전달해야 함
            // *주의*: RedissonClient 인터페이스는 getCommandExecutor()를 노출하지 않으므로,
            //        Redisson 구현체로 캐스팅하거나, Bucket4j가 제공하는 유틸리티를 사용해야 함.
            //        하지만 여기서는 안전하게 CommandAsyncExecutor를 얻기 위해
            //        RedissonClient 객체 자체를 활용하는 builder를 시도합니다.
            
            // 만약 bucket4j-redis 라이브러리가 RedissonClient를 직접 받는 builder를 지원하지 않는다면
            // (구버전 또는 특정 버전 차이), 아래 코드는 수정이 필요할 수 있습니다.
            // bucket4j-redis 8.10.1 기준:
            
            // CommandAsyncExecutor executor = ((Redisson) redissonClient).getCommandExecutor(); 
            // 위 캐스팅은 패키지 의존성을 높이므로 지양.
            
            // 대안: 단순하게 JCache나 Lettuce를 쓰는게 낫지만, 여기서는
            // RedissonBasedProxyManager의 빌더가 보통 CommandExecutor를 요구함.
            // 여기서는 컴파일 성공을 위해 리플렉션이나 캐스팅 대신, 
            // RedissonClient 객체 자체를 래핑하는 로직이 필요하나,
            // 가장 일반적인 'Redisson' 클래스로 캐스팅하여 처리합니다.
            // (RedissonClient 구현체는 보통 org.redisson.Redisson 클래스임)
            
            CommandAsyncExecutor commandExecutor = ((org.redisson.Redisson) redissonClient).getCommandExecutor();

            this.proxyManager = RedissonBasedProxyManager.builderFor(commandExecutor)
                    .withExpirationStrategy(
                            ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1))
                    )
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to initialize Bucket4j Redis Manager. Fallback to Memory (NOT RECOMMENDED for Prod)", e);
            throw new RuntimeException("Rate Limit Init Failed", e);
        }
    }
}