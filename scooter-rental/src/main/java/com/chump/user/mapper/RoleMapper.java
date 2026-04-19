package com.chump.user.mapper;

import com.chump.user.dto.command.CreateRoleCommand;
import com.chump.user.dto.request.CreateRoleRequest;
import com.chump.user.dto.response.RoleResponse;
import com.chump.user.dto.response.RoleWithScopesResponse;
import com.chump.user.model.Role;
import com.chump.user.model.Scope;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = ScopeMapper.class)
public interface RoleMapper {

    RoleResponse toResponse(Role entity);
    List<RoleResponse> toResponseList(List<Role> entityList);
    RoleWithScopesResponse toResponseWithScopes(Role entity);

    @Mapping(source = "command.name", target = "name")
    @Mapping(source = "scopes", target = "scopes")
    Role toEntity(CreateRoleCommand command, List<Scope> scopes);

    CreateRoleCommand toCommand(CreateRoleRequest request);
}
