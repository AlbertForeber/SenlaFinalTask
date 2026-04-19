package com.chump.user.dto.command;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateRoleCommand {

    private String name;
    private List<Integer> scopeIds;
}
