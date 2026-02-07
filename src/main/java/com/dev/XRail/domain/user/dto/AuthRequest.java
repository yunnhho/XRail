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

        @com.dev.XRail.common.annotation.Honeypot
        private String _hidden_check;
    }

    @Getter @Setter
    public static class SignUp {
        @NotBlank private String loginId;
        @NotBlank private String password;
        @NotBlank private String name;
        @NotBlank private String phone;
        @NotBlank private String email;
        private String birthDate;

        @com.dev.XRail.common.annotation.Honeypot
        private String _hidden_check;
    }

    @Getter @Setter
    public static class GuestRegister {
        @NotBlank private String name;
        @NotBlank private String phone;
        @NotBlank(message = "비밀번호 6자리를 입력해주세요.")
        private String password;
    }

    @Getter @Setter
    public static class GuestLogin {
        @NotBlank(message = "예매번호(Access Code)를 입력해주세요.")
        private String accessCode;
        @NotBlank(message = "비밀번호 6자리를 입력해주세요.")
        private String password;
    }
}