package com.chump.user.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserRoleResponse {

    private String username;
    private RoleResponse role;
}
