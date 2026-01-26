package com.dev.XRail.domain.user.service;

import com.dev.XRail.common.dto.TokenResponse;
import com.dev.XRail.domain.user.dto.AuthRequest;
import com.dev.XRail.domain.user.entity.Member;
import com.dev.XRail.domain.user.entity.NonMember;
import com.dev.XRail.domain.user.repository.MemberRepository;
import com.dev.XRail.domain.user.repository.UserRepository;
import com.dev.XRail.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    // AuthenticationManagerBuilder는 Spring Security의 정석적인 로그인 처리를 위해 사용
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    // [회원가입]
    @Transactional
    public Long signUp(AuthRequest.SignUp dto) {
        if (memberRepository.existsByLoginId(dto.getLoginId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        Member member = Member.builder()
                .loginId(dto.getLoginId())
                .password(passwordEncoder.encode(dto.getPassword())) // 암호화 필수
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .birthDate(dto.getBirthDate())
                .build();

        return userRepository.save(member).getId();
    }

    // [회원 로그인]
    @Transactional
    public TokenResponse login(AuthRequest.Login dto) {
        // 1. Login ID/PW 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(dto.getLoginId(), dto.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)
        // CustomUserDetailsService가 필요하지만, 일단 약식으로 진행 (원리 동일)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        return jwtTokenProvider.generateToken(authentication);
    }
}