package com.dev.XRail.domain.user.dto;

import com.dev.XRail.domain.user.entity.SocialProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class AuthRequest {

    @Getter @Setter
    public static class Login {
        @NotBlank(message = "아이디를 입력해주세요.")
        private String loginId;
        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
    }

    @Getter @Setter
    public static class SignUp {
        @NotBlank private String loginId;
        @NotBlank private String password;
        @NotBlank private String name;
        @NotBlank private String phone;
        @NotBlank private String email;
        private String birthDate;
    }

    @Getter @Setter
    public static class NonMemberLogin {
        @NotBlank private String name;
        @NotBlank(message = "발권 시 부여받은 임시 비밀번호 4자리")
        private String password; // 4자리 숫자
        @NotBlank(message = "발권 시 부여받은 예매 번호")
        private String accessCode;
    }
}