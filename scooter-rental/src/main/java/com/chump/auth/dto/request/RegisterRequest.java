package com.chump.auth.dto.request;

import com.chump.common.validation.Trimmed;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class RegisterRequest {

    @Trimmed(message = "Field 'username' must not contain trailing spaces")
    @NotNull(message = "Field 'username' must not be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 to 20 characters long")
    private String username;

    @NotBlank(message = "Field 'password' must not be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "Field 'dateOfBirth' must not be empty")
    @Past(message = "Date of birth must be in past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Field 'email' must not be empty")
    @Size(min = 4, max = 100, message = "Email must be between 6 and 100 characters long")
    @Email(message = "Field 'email' must contain well-formed email address")
    private String email;
}