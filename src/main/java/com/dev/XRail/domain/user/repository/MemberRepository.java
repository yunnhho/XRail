package com.dev.XRail.domain.user.repository;

import com.dev.XRail.domain.user.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId); // 로그인용
    boolean existsByLoginId(String loginId); // 중복 가입 방지
}