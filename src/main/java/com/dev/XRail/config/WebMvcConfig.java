package com.dev.XRail.config;

import com.dev.XRail.common.interceptor.QueueInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final QueueInterceptor queueInterceptor;
    private final com.dev.XRail.common.interceptor.RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. Rate Limiting (가장 먼저 실행)
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**") // API 요청에만 적용
                .order(1);

        // 2. Queue Token Check (대기열 통과 후 진입 가능)
        registry.addInterceptor(queueInterceptor)
                .addPathPatterns("/api/reservations/**", "/api/seats/**", "/api/schedules/**/seats") // 주요 예매 API
                .excludePathPatterns("/api/queue/**", "/api/auth/**") // 대기열 발급/조회, 로그인은 제외
                .order(2);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}