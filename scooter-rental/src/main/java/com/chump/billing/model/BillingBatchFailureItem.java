package com.chump.billing.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "billing_batch_failure_items")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BillingBatchFailureItem {

    @EmbeddedId
    private BillingBatchFailureItemId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("failureId")
    private BillingBatchFailure failure;
}
