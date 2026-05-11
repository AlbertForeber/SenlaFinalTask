package com.chump.auth.dto.request;

import com.chump.common.validation.Trimmed;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginRequest {

    @Trimmed(message = "Field 'username' must not contain trailing spaces")
    @NotBlank(message = "Field 'username' must not be empty")
    private String username;

    @NotBlank(message = "Field 'password' must not be empty")
    private String password;
}
