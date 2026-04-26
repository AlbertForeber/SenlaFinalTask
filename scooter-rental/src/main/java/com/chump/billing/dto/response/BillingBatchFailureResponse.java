package com.chump.billing.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class BillingBatchFailureResponse {

    private int id;
    private Instant failedAt;
    private String errorMessage;
    private Boolean isResolved;
    private List<Integer> subscriptionFailedIds;
}
