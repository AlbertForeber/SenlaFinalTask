package com.chump.billing.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class BillingResponse {

    private int totalSuccess;
    private int totalFailed;
    private List<ManualBillingFailureResponse> failedDetails;
}