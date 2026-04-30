package com.chump.rental.mapper;

import com.chump.rental.dto.command.CreateScooterCommand;
import com.chump.rental.dto.command.UpdateScooterInfoCommand;
import com.chump.rental.dto.entry.TelemetryEntry;
import com.chump.rental.dto.entry.WaypointEntry;
import com.chump.rental.dto.request.CreateScooterRequest;
import com.chump.rental.dto.request.UpdateScooterInfoRequest;
import com.chump.rental.dto.response.ScooterResponse;
import com.chump.rental.kafka.event.TelemetryEvent;
import com.chump.rental.kafka.event.WaypointEvent;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.ScooterModel;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = ScooterModelMapper.class)
public interface ScooterMapper {

    @Mapping(source = "command.serialNumber", target = "serialNumber")
    @Mapping(source = "command.battery", target = "battery")
    @Mapping(source = "command.location", target = "location")
    @Mapping(source = "command.status", target = "status")
    @Mapping(source = "model", target = "model")
    @Mapping(target = "id", ignore = true)
    Scooter toEntity(CreateScooterCommand command, ScooterModel model);

    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "battery", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(source = "model", target = "model")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateScooterInfoFromCommand(UpdateScooterInfoCommand command, ScooterModel model, @MappingTarget Scooter scooter);

    CreateScooterCommand toCreateCommand(CreateScooterRequest request);
    UpdateScooterInfoCommand toUpdateCommand(UpdateScooterInfoRequest request);

    ScooterResponse toResponse(Scooter entity);
    List<ScooterResponse> toResponseList(List<Scooter> entityList);

    @Mapping(target = "latitude", expression = "java(event.getLocation().getY())")
    @Mapping(target = "longitude", expression = "java(event.getLocation().getX())")
    TelemetryEntry toTelemetryEntry(TelemetryEvent event);

    WaypointEntry toWaypointEntry(WaypointEvent event);
}
