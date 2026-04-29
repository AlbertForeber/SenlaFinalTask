package com.chump.billing.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "billing_batch_failures")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BillingBatchFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Без CASCADE read-only, позволяет использовать batch insert для failureItems
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "failure")
    private List<BillingBatchFailureItem> failureItems;

    @Column(name = "failed_at", nullable = false)
    private Instant failedAt;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved;
}
