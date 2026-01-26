package com.dev.XRail.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_login_id", columnNames = "login_id"),
        @UniqueConstraint(name = "uk_member_email", columnNames = "email")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends User {

    @Column(name = "login_id", length = 50, unique = true)
    private String loginId; // 로그인 ID (소셜은 providerId 활용 가능)

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "birth_date", length = 8)
    private String birthDate; // YYYYMMDD

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", length = 20)
    private SocialProvider socialProvider;

    @Column(name = "social_id", length = 100)
    private String socialId; // 소셜 식별값

    @Builder
    public Member(String loginId, String password, String name, String email, String phone,
                  String birthDate, SocialProvider socialProvider, String socialId) {
        super(UserRole.MEMBER);
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.socialProvider = socialProvider != null ? socialProvider : SocialProvider.NONE;
        this.socialId = socialId;
    }
}