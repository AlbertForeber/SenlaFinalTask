package com.chump.user.mapper;

import com.chump.user.dto.response.RoleResponse;
import com.chump.user.model.Role;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toResponse(Role entity);
    List<RoleResponse> toResponseList(List<Role> entityList);
}
