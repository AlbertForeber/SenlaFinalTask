package com.chump.rental.mapper;

import com.chump.rental.dto.command.ScooterCommand;
import com.chump.rental.dto.command.UpdateScooterInfoCommand;
import com.chump.rental.dto.response.ScooterResponse;
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
    Scooter toEntity(ScooterCommand command, ScooterModel model);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateScooterInfoFromCommand(UpdateScooterInfoCommand command, ScooterModel model, @MappingTarget Scooter scooter);

    ScooterResponse toResponse(Scooter entity);
    List<ScooterResponse> toResponseList(List<Scooter> entityList);
}
