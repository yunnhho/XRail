package com.dev.XRail.domain.user.controller;

import com.dev.XRail.common.dto.TokenResponse;
import com.dev.XRail.domain.user.dto.AuthRequest;
import com.dev.XRail.domain.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Long> signUp(@RequestBody @Valid AuthRequest.SignUp request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid AuthRequest.Login request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 비회원 예매 시작 (등록)
    @PostMapping("/guest/register")
    public ResponseEntity<TokenResponse> registerGuest(@RequestBody @Valid AuthRequest.GuestRegister request) {
        return ResponseEntity.ok(authService.registerGuest(request));
    }

    // 비회원 조회 (로그인)
    @PostMapping("/guest/login")
    public ResponseEntity<TokenResponse> loginGuest(@RequestBody @Valid AuthRequest.GuestLogin request) {
        return ResponseEntity.ok(authService.loginGuest(request));
    }
}