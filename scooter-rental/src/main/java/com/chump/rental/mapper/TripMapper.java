package com.chump.rental.mapper;

import com.chump.rental.dto.RouteData;
import com.chump.rental.dto.response.TripDetailedResponse;
import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.model.Trip;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = TripPointMapper.class)
public interface TripMapper {

    @Mapping(source = "scooter.id", target = "scooterId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "avgSpeedKmh", expression = "java(entity.getDistance() != null && " +
            "entity.getDurationSeconds() != null ? entity.getDistance() / entity.getDurationSeconds() * 3.6 : null)")
    TripDetailedResponse toDetailedResponse(Trip entity);

    @Mapping(source = "scooter.id", target = "scooterId")
    @Mapping(source = "user.id", target = "userId")
    TripConciseResponse toConciseResponse(Trip entity);

    List<TripConciseResponse> toConsiseResponseList(List<Trip> entityList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTripFromRouteData(RouteData routeData, @MappingTarget Trip trip);
}
