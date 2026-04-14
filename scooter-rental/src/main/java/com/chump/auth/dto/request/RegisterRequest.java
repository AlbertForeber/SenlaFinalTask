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

    @NotBlank(message = "Username field should not be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 to 20 symbols long")
    private String username;

    @NotBlank(message = "Password field should npt be empty")
    @Size(min = 8, message = "Password must be at least 8 symbols long")
    private String password;

    @NotNull(message = "Date of birth is necessary for registration")
    private LocalDate dateOfBirth;

    @NotNull(message = "Full name is necessary for registration")
    private String fullName;
}