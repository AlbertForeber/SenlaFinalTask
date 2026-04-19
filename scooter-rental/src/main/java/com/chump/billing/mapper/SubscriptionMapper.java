package com.chump.billing.mapper;

import com.chump.billing.dto.command.CreateSubscriptionTariffCommand;
import com.chump.billing.dto.request.CreateSubscriptionTariffRequest;
import com.chump.billing.dto.response.CurrentSubscriptionResponse;
import com.chump.billing.dto.response.SubscribedResponse;
import com.chump.billing.dto.response.SubscriptionTariffResponse;
import com.chump.billing.model.SubscriptionTariff;
import com.chump.billing.model.Tariff;
import com.chump.user.model.UserSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TariffMapper.class)
public interface SubscriptionMapper {

    @Mapping(source = "tariff.basePrice", target = "instantPay")
    SubscribedResponse toSubscribedResponse(UserSubscription entity);

    @Mapping(source = "tariff.name", target = "name")
    @Mapping(source = "tariff.basePrice", target = "basePrice")
    SubscriptionTariffResponse toSubscriptionResponse(SubscriptionTariff entity);

    @Mapping(source = "tariff", target = "tariff")
    CurrentSubscriptionResponse toCurrentSubscriptionResponse(UserSubscription subscription, SubscriptionTariff tariff);

    CreateSubscriptionTariffCommand toCreateCommand(CreateSubscriptionTariffRequest request);

    @Mapping(source = "command.durationDays", target = "durationDays")
    @Mapping(source = "tariff", target = "tariff")
    @Mapping(source = "tariff.id", target = "id")
    SubscriptionTariff toEntity(CreateSubscriptionTariffCommand command, Tariff tariff);
}
