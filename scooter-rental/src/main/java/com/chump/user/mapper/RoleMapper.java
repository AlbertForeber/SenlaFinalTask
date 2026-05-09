package com.chump.user.mapper;

import com.chump.user.dto.command.RoleCommand;
import com.chump.user.dto.request.CreateRoleRequest;
import com.chump.user.dto.request.UpdateRoleRequest;
import com.chump.user.dto.response.RoleResponse;
import com.chump.user.dto.response.RoleWithScopesResponse;
import com.chump.user.model.Role;
import com.chump.user.model.Scope;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = ScopeMapper.class)
public interface RoleMapper {

    RoleResponse toResponse(Role entity);
    List<RoleResponse> toResponseList(List<Role> entityList);
    RoleWithScopesResponse toResponseWithScopes(Role entity);

    @Mapping(source = "command.name", target = "name")
    @Mapping(source = "scopes", target = "scopes")
    @Mapping(target = "id", ignore = true) // id не назначается вручную
    Role toEntity(RoleCommand command, List<Scope> scopes);

    RoleCommand toCreateCommand(CreateRoleRequest request);
    RoleCommand toUpdateCommand(UpdateRoleRequest request);
}
