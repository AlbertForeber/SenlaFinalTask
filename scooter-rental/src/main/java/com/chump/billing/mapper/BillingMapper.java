package com.chump.billing.mapper;

import com.chump.billing.dto.response.BillingBatchFailureResponse;
import com.chump.billing.model.BillingBatchFailure;
import com.chump.billing.model.BillingBatchFailureItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BillingMapper {

    @Mapping(source = "failureItems", target = "subscriptionFailedIds")
    BillingBatchFailureResponse toResponse(BillingBatchFailure entity);

    List<BillingBatchFailureResponse> toResponseList(List<BillingBatchFailure> entityList);

    default List<Integer> failureItemsToSubscriptionIds(List<BillingBatchFailureItem> items) {
        return items.stream().map(o -> o.getId().getUserSubscriptionId()).toList();
    }
}
