package com.chump.auth.dto.request;

import com.chump.common.validation.Trimmed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginRequest {

    @Trimmed(message = "Field 'username' must not contain trailing spaces")
    @NotNull(message = "Field 'username' must not be empty")
    private String username;

    @NotBlank(message = "Field 'password' must not be empty")
    private String password;
}
