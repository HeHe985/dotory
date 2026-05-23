package com.dotory.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;
}
