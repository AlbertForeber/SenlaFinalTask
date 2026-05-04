package com.chump.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LogoutRequest {

    @NotBlank(message = "Field 'refreshToken' must not be empty")
    private String refreshToken;
}
