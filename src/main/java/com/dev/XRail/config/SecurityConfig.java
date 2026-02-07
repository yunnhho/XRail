package com.dev.XRail.config;

import com.dev.XRail.security.jwt.JwtAuthenticationFilter;
import com.dev.XRail.security.jwt.JwtTokenProvider;
import com.dev.XRail.security.oauth.CustomOAuth2UserService;
import com.dev.XRail.security.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
            .csrf(AbstractHttpConfigurer::disable) // REST API라 CSRF 불필요
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 사용
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**", "/api/schedules/**", "/api/stations/**", "/api/admin/**").permitAll() // 로그인, 회원가입, 스케쥴, 역 정보, 관리자 조회 개방
                    .requestMatchers("/actuator/**").permitAll() // 모니터링 개방
                    .anyRequest().authenticated() // 나머지는 인증 필요
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            .formLogin(AbstractHttpConfigurer::disable)
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // 모든 Origin 허용 (보안 필요 시 특정 도메인으로 제한)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    /* 테스트용
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // [Test Mode] 부하 테스트를 위해 모든 API 허용 (운영 시 반드시 원복 필요)
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated()
                );
        // .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터가 있어도 permitAll이면 통과

        return http.build();
    }
    */

}