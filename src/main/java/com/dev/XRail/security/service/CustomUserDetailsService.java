package com.dev.XRail.security.service;

import com.dev.XRail.domain.user.entity.Member;
import com.dev.XRail.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        return memberRepository.findByLoginId(loginId)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("해당 아이디의 회원을 찾을 수 없습니다: " + loginId));
    }

    // DB Member -> Spring Security User 변환
    private UserDetails createUserDetails(Member member) {
        return new User(
                member.getLoginId(), // Principal (ID)
                member.getPassword(), // Credential (PW)
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );
    }
}