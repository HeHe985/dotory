package com.dotory.domain.auth.service;

import com.dotory.domain.auth.dto.response.LoginResponse;
import com.dotory.domain.auth.jwt.JwtProvider;
import com.dotory.domain.user.entity.User;
import com.dotory.domain.user.repository.UserRepository;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    public static final String REFRESH_TOKEN = "RefreshToken:";
    public static final String LOGOUT = "logout";

    // OAuth 로그인 성공 후 호출할 토큰 발급 메서드
    public LoginResponse login(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        setInRedis(
                REFRESH_TOKEN + user.getId(),
                refreshToken,
                jwtProvider.getRefreshExpiration()
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    public void logout(String accessToken) {
        if (!jwtProvider.validateToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
        }

        long remainingTime = jwtProvider.getRemainingExpirationTime(accessToken);

        if (remainingTime > 0) {
            setInRedis(accessToken, LOGOUT, remainingTime);
        }
    }

    public LoginResponse refresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        UUID userId = jwtProvider.getUserId(refreshToken);

        validateRefreshToken(refreshToken, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());

        return new LoginResponse(newAccessToken, refreshToken);
    }

    private void validateRefreshToken(String refreshToken, UUID userId) {
        String savedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN + userId);

        if (ObjectUtils.isEmpty(savedToken) || !savedToken.equals(refreshToken)) {
            throw new IllegalArgumentException("만료되었거나 일치하지 않는 Refresh Token입니다.");
        }
    }

    private void setInRedis(String key, String value, long timeout) {
        redisTemplate.opsForValue().set(
                key,
                value,
                timeout,
                TimeUnit.MILLISECONDS
        );
    }

}
