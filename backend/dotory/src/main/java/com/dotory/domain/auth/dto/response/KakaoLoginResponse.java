package com.dotory.domain.auth.dto.response;

import com.dotory.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoLoginResponse {

    private User user;
    private String kakaoAccessToken;
}
