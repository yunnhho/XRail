package com.dev.XRail.domain.user.repository;

import com.dev.XRail.domain.user.entity.NonMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NonMemberRepository extends JpaRepository<NonMember, Long> {
    Optional<NonMember> findByAccessCode(String accessCode);
}
