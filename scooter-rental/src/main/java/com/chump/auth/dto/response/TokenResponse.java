package com.chump.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
}
