package com.chump.billing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class BillingBatchFailureItemId {

    @Column(name = "failure_id")
    private Integer failureId;

    @Column(name = "user_subscription_id")
    private Integer userSubscriptionId;
}