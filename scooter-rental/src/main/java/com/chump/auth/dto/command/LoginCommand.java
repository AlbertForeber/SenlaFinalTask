package com.chump.auth.dto.command;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginCommand {

    private String username;
    private String password;
}
