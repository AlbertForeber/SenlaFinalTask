package com.chump.rental.mapper;

import com.chump.rental.dto.response.ScooterModelResponse;
import com.chump.rental.model.ScooterModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScooterModelMapper {

    ScooterModelResponse toResponse(ScooterModel entity);
    List<ScooterModelResponse> toResponseList(List<ScooterModel> entityList);
}
