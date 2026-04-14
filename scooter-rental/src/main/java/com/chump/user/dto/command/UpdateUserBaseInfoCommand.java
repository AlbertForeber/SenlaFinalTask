package com.chump.user.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class UpdateUserBaseInfoCommand {

    private String fullName;
    private LocalDate dateOfBirth;
}