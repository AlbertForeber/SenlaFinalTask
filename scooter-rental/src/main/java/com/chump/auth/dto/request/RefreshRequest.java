package com.chump.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {

    @NotBlank(message = "Field 'oldRefreshToken' must not be empty")
    private String oldRefreshToken;
}
