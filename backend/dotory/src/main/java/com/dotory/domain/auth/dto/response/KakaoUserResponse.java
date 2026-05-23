package com.dotory.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoUserResponse {

    private Long id;

    // [수정] kakao_account 대신 properties 사용
    private Properties properties;

    @Getter
    public static class Properties {

        private String nickname;

        @JsonProperty("profile_image")
        private String profileImage;
    }
}
