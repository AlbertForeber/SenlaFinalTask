package com.chump.billing.controller;

import com.chump.billing.dto.response.BillingBatchFailureResponse;
import com.chump.billing.dto.response.BillingResponse;
import com.chump.billing.service.BillingService;
import com.chump.billing.service.query.BillingFailureQueryService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@Validated // TODO добавить маппинг везде
public class BillingController {

    private final BillingFailureQueryService billingFailureQueryService;
    private final BillingService billingService;

    public BillingController(BillingFailureQueryService billingFailureQueryService, BillingService billingService) {
        this.billingFailureQueryService = billingFailureQueryService;
        this.billingService = billingService;
    }

    @GetMapping("/failures")
    public ResponseEntity<List<BillingBatchFailureResponse>> getFailures(
            @RequestParam(defaultValue = "10", required = false)
            @Positive(message = "Param 'pageSize' must be positive number")
            int pageSize,

            @RequestParam(defaultValue = "1", required = false)
            @Positive(message = "Param 'page' must be positive number")
            int page
    ) {
        return ResponseEntity.ok(billingFailureQueryService.getAllBillingFailures(pageSize, page));
    }

    @PostMapping("/manual")
    public ResponseEntity<BillingResponse> postManualBatching(
            @RequestParam(defaultValue = "false", required = false) boolean failedOnly
    ) {
        return ResponseEntity.ok(billingService.manualBilling(failedOnly));
    }
}
