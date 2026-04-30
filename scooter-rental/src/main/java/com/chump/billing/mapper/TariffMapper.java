package com.chump.billing.mapper;

import com.chump.billing.dto.command.CreateSubscriptionTariffCommand;
import com.chump.billing.dto.command.TariffCommand;
import com.chump.billing.dto.request.CreateTariffRequest;
import com.chump.billing.dto.request.UpdateTariffRequest;
import com.chump.billing.dto.response.TariffConciseResponse;
import com.chump.billing.dto.response.TariffDetailedResponse;
import com.chump.billing.model.Tariff;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TariffMapper {

    @Mapping(target = "billingIntervalMinutes",
            expression = "java(entity.getBillingIntervalMinutes() != null ? " +
                    "entity.getBillingIntervalMinutes().toString() : \"SUBSCRIPTION\")")
    TariffDetailedResponse toDetailedResponse(Tariff entity);
    TariffConciseResponse toConciseResponse(Tariff entity);
    List<TariffConciseResponse> toConciseResponseList(List<Tariff> entityList);

    TariffCommand toUpdateCommand(UpdateTariffRequest request);
    TariffCommand toCreateCommand(CreateTariffRequest request);

    @Mapping(source = "command.interval", target = "billingIntervalMinutes")
    @Mapping(target = "id", ignore = true)
    Tariff toEntity(TariffCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "billingIntervalMinutes", ignore = true) // Для подписки не нужно
    Tariff toEntityForSubscription(CreateSubscriptionTariffCommand command);

    @InheritConfiguration
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTariffFromCommand(TariffCommand command, @MappingTarget Tariff entity);
}
