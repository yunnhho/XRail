package com.dev.XRail.domain.user.repository;

import com.dev.XRail.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}