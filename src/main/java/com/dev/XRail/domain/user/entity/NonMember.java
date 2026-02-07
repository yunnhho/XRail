package com.dev.XRail.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "non_members", indexes = {
        @Index(name = "idx_non_member_access", columnList = "access_code")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NonMember extends User {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String password; // 6자리 숫자 (발권 확인용) - 암호화 저장됨

    @Column(name = "access_code", nullable = false, length = 10, unique = true)
    private String accessCode; // 고정길이 랜덤 문자열 (티켓 조회 Key)

    @Builder
    public NonMember(String name, String phone, String password, String accessCode) {
        super(UserRole.NON_MEMBER);
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.accessCode = accessCode;
    }
}