package com.dev.XRail.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TokenResponse {
    private String grantType;    // Bearer
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
    
    // 비회원 예매 시 발급되는 예매번호 (회원 로그인 시에는 null)
    private String accessCode;
}