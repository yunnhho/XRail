package com.dev.XRail.infra.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisScriptConfig {

    @Bean
    public RedisScript<Long> reservationScript() {
        // 리턴 타입을 Long으로 명시 (Lua에서 1 또는 0 반환)
        return RedisScript.of(new ClassPathResource("scripts/reserve_seat.lua"), Long.class);
    }

    @Bean
    public RedisScript<Long> rollbackScript() {
        return RedisScript.of(new ClassPathResource("scripts/rollback_seat.lua"), Long.class);
    }
}