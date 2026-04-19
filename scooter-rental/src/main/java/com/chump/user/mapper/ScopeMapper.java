package com.chump.user.mapper;

import com.chump.user.dto.response.ScopeResponse;
import com.chump.user.model.Scope;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScopeMapper {

    ScopeResponse toResponse(Scope entity);
}
