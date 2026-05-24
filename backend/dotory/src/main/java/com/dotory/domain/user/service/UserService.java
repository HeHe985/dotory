package com.dotory.domain.user.service;

import com.dotory.common.exception.ApiException;
import com.dotory.common.exception.errorcode.UserErrorCode;
import com.dotory.domain.user.dto.request.SignupRequest;
import com.dotory.domain.user.entity.User;
import com.dotory.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request) {
        // 이메일 중복 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(UserErrorCode.EXIST_EMAIL);
        }

        // 비밀번호 단방향 암호화 (Bcrypt)
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 엔티티 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .provider(null)
                .providerId(null)
                .build();

        // DB에 저장
        userRepository.save(user);
    }
}
