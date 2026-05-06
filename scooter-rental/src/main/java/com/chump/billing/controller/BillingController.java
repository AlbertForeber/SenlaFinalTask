package com.chump.billing.controller;

import com.chump.billing.dto.response.BillingBatchFailureResponse;
import com.chump.billing.dto.response.BillingResponse;
import com.chump.billing.service.BillingService;
import com.chump.billing.service.query.BillingFailureQueryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@Validated // TODO добавить маппинг везде
@RequiredArgsConstructor
public class BillingController {

    private final BillingFailureQueryService billingFailureQueryService;
    private final BillingService billingService;

    @GetMapping("/failures")
    @PreAuthorize("hasAuthority('SCOPE_billing:view_admin')")
    public ResponseEntity<List<BillingBatchFailureResponse>> getFailures(
            @RequestParam(defaultValue = "10", required = false)
            @Positive(message = "Param 'pageSize' must not be negative")
            int pageSize,

            @RequestParam(defaultValue = "1", required = false)
            @Positive(message = "Param 'page' must not be negative")
            int page
    ) {
        return ResponseEntity.ok(billingFailureQueryService.getAllBillingFailures(pageSize, page));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAuthority('SCOPE_billing:manage_admin')")
    public ResponseEntity<BillingResponse> postManualBatching(
            @RequestParam(defaultValue = "false", required = false) boolean failedOnly
    ) {
        return ResponseEntity.ok(billingService.manualBilling(failedOnly));
    }
}
