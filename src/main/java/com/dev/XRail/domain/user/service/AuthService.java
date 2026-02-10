package com.dev.XRail.domain.user.service;

import com.dev.XRail.common.dto.TokenResponse;
import com.dev.XRail.domain.user.dto.AuthRequest;
import com.dev.XRail.domain.user.entity.Member;
import com.dev.XRail.domain.user.entity.NonMember;
import com.dev.XRail.domain.user.entity.UserRole;
import com.dev.XRail.domain.user.repository.MemberRepository;
import com.dev.XRail.domain.user.repository.NonMemberRepository;
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
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final NonMemberRepository nonMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    // [회원가입]
    @Transactional
    public Long signUp(AuthRequest.SignUp dto) {
        if (memberRepository.existsByLoginId(dto.getLoginId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        Member member = Member.builder()
                .loginId(dto.getLoginId())
                .password(passwordEncoder.encode(dto.getPassword()))
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
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(dto.getLoginId(), dto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        return jwtTokenProvider.generateToken(authentication);
    }

    // [비회원 예매 시작 (등록)]
    @Transactional
    public TokenResponse registerGuest(AuthRequest.GuestRegister dto) {
        String accessCode = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        // 비회원 비밀번호도 암호화하여 저장
        NonMember nonMember = NonMember.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .password(passwordEncoder.encode(dto.getPassword()))
                .accessCode(accessCode)
                .build();
        
        nonMemberRepository.save(nonMember);

        // JWT 발급을 위한 Authentication 객체 수동 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                accessCode, // Principal (여기선 AccessCode를 ID로 사용)
                null, 
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.NON_MEMBER.name()))
        );

        TokenResponse tokenResponse = jwtTokenProvider.generateToken(authentication);
        tokenResponse.setAccessCode(accessCode); // TokenResponse에 setter나 필드 추가 필요
        return tokenResponse;
    }

    // [비회원 조회 (로그인)]
    @Transactional
    public TokenResponse loginGuest(AuthRequest.GuestLogin dto) {
        NonMember nonMember = nonMemberRepository.findByAccessCode(dto.getAccessCode())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 예매번호입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), nonMember.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                nonMember.getAccessCode(),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.NON_MEMBER.name()))
        );

        return jwtTokenProvider.generateToken(authentication);
    }
}