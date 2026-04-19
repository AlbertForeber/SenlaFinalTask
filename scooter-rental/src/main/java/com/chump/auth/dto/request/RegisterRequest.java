package com.chump.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class RegisterRequest {

    @NotBlank(message = "Field 'username' must not be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 to 20 characters long")
    private String username;

    @NotBlank(message = "Field 'password' must not be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "Field 'dateOfBirth' must not be empty")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Field 'fullName' must not be empty")
    private String fullName;
}