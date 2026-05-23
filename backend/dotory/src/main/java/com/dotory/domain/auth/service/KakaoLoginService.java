package com.dotory.domain.auth.service;

import com.dotory.domain.auth.dto.response.KakaoTokenResponse;
import com.dotory.domain.auth.dto.response.KakaoUserResponse;
import com.dotory.domain.auth.dto.response.LoginResponse;
import com.dotory.domain.user.entity.SocialProvider;
import com.dotory.domain.user.entity.User;
import com.dotory.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    private final RestClient restClient = RestClient.create();

    public String getKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;
    }

    @Transactional
    public LoginResponse login(String code) {
        KakaoTokenResponse tokenResponse = getToken(code);
        KakaoUserResponse kakaoUser = getUserInfo(tokenResponse.getAccessToken());

        String providerId = String.valueOf(kakaoUser.getId());

        User user = userRepository.findByProviderAndProviderId(
                        SocialProvider.KAKAO,
                        providerId
                )
                .orElseGet(() -> userRepository.save(
                        User.createSocialUser(
                                null,
                                kakaoUser.getProperties().getNickname(),
                                kakaoUser.getProperties().getProfileImage(),
                                SocialProvider.KAKAO,
                                providerId
                        )
                ));

        return authService.login(user);
    }

    private KakaoTokenResponse getToken(String code) {

        // [수정] 문자열 body 대신 MultiValueMap으로 form-urlencoded 요청 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);

        // [추가] Client Secret을 ON으로 사용할 경우 필수
        params.add("client_secret", clientSecret);

        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        return restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)

                // [수정] 문자열이 아니라 params를 body로 전달
                .body(params)

                .retrieve()
                .body(KakaoTokenResponse.class);
    }

    private KakaoUserResponse getUserInfo(String accessToken) {
        return restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserResponse.class);
    }
}
