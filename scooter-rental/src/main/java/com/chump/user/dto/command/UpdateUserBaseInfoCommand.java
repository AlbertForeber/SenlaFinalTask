package com.chump.user.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class UpdateUserBaseInfoCommand {

    private String email;
    private LocalDate dateOfBirth;
}