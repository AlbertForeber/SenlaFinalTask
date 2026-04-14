package com.chump.rental.mapper;

import com.chump.common.mapper.GeometryMapper;
import com.chump.rental.dto.command.RentalSpotCommand;
import com.chump.rental.dto.response.RentalSpotConciseResponse;
import com.chump.rental.dto.response.RentalSpotDetailedResponse;
import com.chump.rental.dto.response.RentalSpotHierarchyResponse;
import com.chump.rental.dto.response.RentalSpotWithScootersResponse;
import com.chump.rental.model.RentalSpot;
import com.chump.rental.model.Scooter;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ScooterMapper.class, GeometryMapper.class})
public interface RentalSpotMapper {

    RentalSpotHierarchyResponse toHierarchyResponse(RentalSpot entity);
    RentalSpotConciseResponse toConciseResponse(RentalSpot entity);

    @Mapping(source = "parent.id", target = "parentId")
    RentalSpotDetailedResponse toDetailedResponse(RentalSpot entity);

    List<RentalSpotConciseResponse> toConciseResponseList(List<RentalSpot> entity);

    @Mapping(source = "spot.id", target = "id")
    @Mapping(source = "spot.name", target = "name")
    @Mapping(source = "scooters", target = "scootersInSpot")
    @Mapping(source = "spot.parent.id", target = "parentId")
    RentalSpotWithScootersResponse toWithScootersResponse(RentalSpot spot, List<Scooter> scooters);

    @Mapping(source = "parent", target = "parent")
    @Mapping(source = "command.name", target = "name")
    @Mapping(source = "command.area", target = "area")
    @Mapping(source = "command.isZone", target = "isZone")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "children", ignore = true)
    RentalSpot toEntity(RentalSpotCommand command, RentalSpot parent);

    @InheritConfiguration
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRentalPointFromCommand(RentalSpotCommand command,
                                      RentalSpot parent,
                                      @MappingTarget RentalSpot entity);
}