package com.chump.user.mapper;

import com.chump.user.dto.response.ScopeResponse;
import com.chump.user.model.Scope;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScopeMapper {

    ScopeResponse toResponse(Scope entity);
    List<ScopeResponse> toResponseList(List<Scope> entityList);
}
