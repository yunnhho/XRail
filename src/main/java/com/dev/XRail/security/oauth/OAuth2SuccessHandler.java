package com.dev.XRail.security.oauth;

import com.dev.XRail.common.dto.TokenResponse;
import com.dev.XRail.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private static final String REDIRECT_URI = "http://localhost:5173/oauth/callback"; // Frontend URL

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        TokenResponse tokenResponse = jwtTokenProvider.generateToken(authentication);

        String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("accessToken", tokenResponse.getAccessToken())
                .queryParam("refreshToken", tokenResponse.getRefreshToken())
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}
