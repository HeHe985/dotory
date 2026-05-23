package com.dotory.domain.auth.controller;

import com.dotory.common.response.ApiResponse;
import com.dotory.domain.auth.dto.response.LoginResponse;
import com.dotory.domain.auth.service.AuthService;
import com.dotory.domain.auth.service.KakaoLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController {

    private final AuthService authService;
    private final KakaoLoginService kakaoLoginService;

    @GetMapping("/kakao/login-url")
    public ResponseEntity<ApiResponse<String>> getKakaoLoginUrl() {
        return ResponseEntity.ok(ApiResponse.success(kakaoLoginService.getKakaoLoginUrl()));
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoCallback(@RequestParam String code) {
        LoginResponse response = kakaoLoginService.login(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        LoginResponse response = authService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        authService.logout(accessToken);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}