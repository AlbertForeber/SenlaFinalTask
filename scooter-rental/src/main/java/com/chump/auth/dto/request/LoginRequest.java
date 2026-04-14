package com.chump.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginRequest {

    @NotBlank(message = "Username field should not be empty")
    private String username;

    @NotBlank(message = "Password field should not be empty")
    private String password;
}
