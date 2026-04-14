package com.chump.rental.mapper;

import com.chump.rental.dto.response.TripPointResponse;
import com.chump.rental.model.TripPoint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripPointMapper {

    @Mapping(source = "id.tripId", target = "tripId")
    @Mapping(source = "id.createdAt", target = "createdAt")
    TripPointResponse toResponse(TripPoint entity);
}
