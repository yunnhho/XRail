package com.dev.XRail.security.service;

import com.dev.XRail.domain.user.entity.Member;
import com.dev.XRail.domain.user.entity.NonMember;

import com.dev.XRail.domain.user.entity.UserRole;

import com.dev.XRail.domain.user.repository.MemberRepository;

import com.dev.XRail.domain.user.repository.NonMemberRepository;

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
    private final NonMemberRepository nonMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 회원 조회
        return memberRepository.findByLoginId(username)
                .map(this::createUserDetails)
                .orElseGet(() ->
                    // 2. 비회원 조회 (AccessCode)
                    nonMemberRepository.findByAccessCode(username)
                            .map(this::createNonMemberUserDetails)
                            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username))
                );
    }
    // DB Member -> Spring Security User 변환

    private UserDetails createUserDetails(Member member) {
        return new User(
                member.getLoginId(),
                member.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );
    }

    // DB NonMember -> Spring Security User 변환
    private UserDetails createNonMemberUserDetails(NonMember nonMember) {
        return new User(
                nonMember.getAccessCode(),
                nonMember.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.NON_MEMBER.name()))
        );
    }
}
