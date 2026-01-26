package com.dev.XRail.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenResponse {
    private String grantType;    // Bearer
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
}