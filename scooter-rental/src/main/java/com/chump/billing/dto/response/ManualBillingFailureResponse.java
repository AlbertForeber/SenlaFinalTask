package com.chump.billing.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ManualBillingFailureResponse {
    private int subscriptionId;
    private String errorMessage;
}
