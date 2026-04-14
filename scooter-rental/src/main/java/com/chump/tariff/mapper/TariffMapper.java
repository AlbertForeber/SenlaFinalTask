package com.chump.tariff.mapper;

import com.chump.tariff.dto.response.TariffResponse;
import com.chump.tariff.model.Tariff;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TariffMapper {

    TariffResponse toResponse(Tariff entity);
    List<TariffResponse> toResponseList(List<Tariff> entityList);
}
