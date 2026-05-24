package com.dotory.domain.auth.service;

import com.dotory.domain.auth.dto.response.KakaoLoginResponse;
import com.dotory.domain.auth.dto.response.KakaoTokenResponse;
import com.dotory.domain.auth.dto.response.KakaoUserResponse;
import com.dotory.domain.user.entity.SocialProvider;
import com.dotory.domain.user.entity.User;
import com.dotory.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    public String getKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;
    }

    @Transactional
    public KakaoLoginResponse loginOrSignup(String code) {
        KakaoTokenResponse tokenResponse = getToken(code);
        KakaoUserResponse userResponse = getUserInfo(tokenResponse.getAccessToken());

        String providerId = String.valueOf(userResponse.getId());

        User user = userRepository.findByProviderAndProviderId(SocialProvider.KAKAO, providerId)
                .orElseGet(() -> signupKakaoUser(userResponse, providerId));

        return new KakaoLoginResponse(user, tokenResponse.getAccessToken());
    }

    private KakaoTokenResponse getToken(String code) {
        return RestClient.create()
                .post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=authorization_code"
                        + "&client_id=" + clientId
                        + "&client_secret=" + clientSecret
                        + "&redirect_uri=" + redirectUri
                        + "&code=" + code)
                .retrieve()
                .body(KakaoTokenResponse.class);
    }

    private KakaoUserResponse getUserInfo(String kakaoAccessToken) {
        return RestClient.create()
                .get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .body(KakaoUserResponse.class);
    }

    private User signupKakaoUser(KakaoUserResponse response, String providerId) {
        KakaoUserResponse.KakaoAccount kakaoAccount = response.getKakaoAccount();

        String email = kakaoAccount != null ? kakaoAccount.getEmail() : null;

        String nickname = null;
        if (kakaoAccount != null && kakaoAccount.getProfile() != null) {
            nickname = kakaoAccount.getProfile().getNickname();
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("KAKAO_USER_" + providerId))
                .name(nickname)
                .provider(SocialProvider.KAKAO)
                .providerId(providerId)
                .build();

        return userRepository.save(user);
    }

    public void logout(String kakaoAccessToken) {
        RestClient.create()
                .post()
                .uri("https://kapi.kakao.com/v1/user/logout")
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .toBodilessEntity();
    }
}
