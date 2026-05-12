package com.chump.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateUserBaseInfoRequest {

    @Size(min = 6, max = 100, message = "Email must be between 6 and 100 characters long")
    @Email(message = "Field 'email' must contain well-formed email address")
    private String email;

    @Past(message = "Date of birth must be in past")
    private LocalDate dateOfBirth;
}