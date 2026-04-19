package com.chump.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateUserBaseInfoRequest {

    @Size(min = 4, max = 100, message = "Full name must be between 4 and 100 characters long")
    private String fullName;

    private LocalDate dateOfBirth;
}