package com.chump.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {

    @NotBlank(message = "Old refresh token field is necessary for refreshing")
    private String oldRefreshToken;
}
