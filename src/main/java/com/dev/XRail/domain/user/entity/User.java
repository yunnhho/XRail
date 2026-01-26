package com.dev.XRail.domain.user.entity;

import com.dev.XRail.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED) // 자식 테이블과 조인 전략 사용
@DiscriminatorColumn(name = "dtype") // 회원 구분 컬럼 (Member/NonMember)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    protected User(UserRole role) {
        this.role = role;
    }
}