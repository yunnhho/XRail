package com.dev.XRail.domain.user.dto;

import com.dev.XRail.common.annotation.Honeypot;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20)
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8)
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email
    private String email;

    private String phone;
    private String birthDate;

    @Honeypot
    private String website; // Honeypot field (hidden from users)
}
