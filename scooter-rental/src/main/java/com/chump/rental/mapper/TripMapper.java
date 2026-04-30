package com.chump.rental.mapper;

import com.chump.rental.dto.response.TripDetailedResponse;
import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.model.Trip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = TripPointMapper.class)
public interface TripMapper {

    @Mapping(source = "scooter.id", target = "scooterId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "distance", expression = "java(entity.getRoute().getLength())")
    TripDetailedResponse toDetailedResponse(Trip entity);

    @Mapping(source = "scooter.id", target = "scooterId")
    @Mapping(source = "user.id", target = "userId")
    TripConciseResponse toConciseResponse(Trip entity);

    List<TripConciseResponse> toConsiseResponseList(List<Trip> entityList);
}
