package com.dotory.domain.auth.controller;

import com.dotory.common.exception.errorcode.CommonErrorCode;
import com.dotory.common.response.ApiResponse;
import com.dotory.domain.auth.dto.request.LoginRequest;
import com.dotory.domain.auth.dto.request.RefreshRequest;
import com.dotory.domain.auth.dto.response.LoginResponse;
import com.dotory.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "로그인 & 로그아웃 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인")
    @SecurityRequirements()
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {

        // 1. 서비스 로직 호출 (이메일, 평문 비밀번호 전달)
        LoginResponse loginResponse = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

        // 2. 작성해두신 공통 응답 객체(ApiResponse)로 access, refresh 토큰 반환
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {

        // 1. 헤더에서 토큰 추출 (Bearer 제외)
        String token = resolveToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(CommonErrorCode.INVALID_INPUT_VALUE));
        }

        // 2. 서비스 호출하여 블랙리스트 등록
        authService.logout(token);

        return ResponseEntity.ok(ApiResponse.success("성공적으로 로그아웃 되었습니다."));
    }

    @Operation(summary = "토큰 재발급 (Access Token 만료 시)")
    @SecurityRequirements()
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(CommonErrorCode.INVALID_INPUT_VALUE));
        }

        LoginResponse loginResponse = authService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Operation(summary = "카카오 로그인 URL 조회")
    @SecurityRequirements()
    @GetMapping("/kakao/login")
    public ResponseEntity<ApiResponse<String>> kakaoLoginUrl() {
        return ResponseEntity.ok(ApiResponse.success(authService.getKakaoLoginUrl()));
    }

    @Operation(summary = "카카오 로그인 콜백")
    @SecurityRequirements()
    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoCallback(@RequestParam String code) {
        LoginResponse response = authService.kakaoLogin(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}