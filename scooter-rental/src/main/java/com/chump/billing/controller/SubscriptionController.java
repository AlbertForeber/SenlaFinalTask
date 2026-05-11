package com.chump.billing.controller;

import com.chump.billing.dto.request.CreateSubscriptionTariffRequest;
import com.chump.billing.dto.request.UpdateSubscriptionTariffRequest;
import com.chump.billing.dto.response.CurrentSubscriptionResponse;
import com.chump.billing.dto.response.SubscribedResponse;
import com.chump.billing.dto.response.SubscriptionTariffResponse;
import com.chump.billing.dto.response.TariffConciseResponse;
import com.chump.billing.mapper.SubscriptionMapper;
import com.chump.billing.service.BillingService;
import com.chump.billing.service.SubscriptionService;
import com.chump.billing.service.query.SubscriptionQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@Validated
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionQueryService subscriptionQueryService;
    private final SubscriptionMapper subscriptionMapper;
    private final BillingService billingService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_tariff:view')")
    public ResponseEntity<List<TariffConciseResponse>> getSubscriptions(
            @Positive(message = "Param 'pageSize' must not be negative")
            @RequestParam(defaultValue = "10", required = false) int pageSize,

            @Positive(message = "Param 'page' must not be negative")
            @RequestParam(defaultValue = "1", required = false) int page
    ) {
        return ResponseEntity.ok(subscriptionQueryService.getAllSubscriptionTariffs(pageSize, page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_tariff:view')")
    public ResponseEntity<SubscriptionTariffResponse> getSubscription(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(subscriptionQueryService.getSubscriptionTariff(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('SCOPE_tariff:view')")
    public ResponseEntity<CurrentSubscriptionResponse> getMySubscription(
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(subscriptionQueryService.getCurrentSubscriptionOfUser(userId));
    }

    @GetMapping(params = "user_id")
    @PreAuthorize("hasAuthority('SCOPE_tariff:view_admin')")
    public ResponseEntity<CurrentSubscriptionResponse> getUserSubscription(
            @Positive(message = "Param 'user_id' must be positive")
            @RequestParam(name = "user_id") Integer userId
    ) {
        return ResponseEntity.ok(subscriptionQueryService.getCurrentSubscriptionOfUser(userId));
    }

    @PostMapping("/{id}/subscribe")
    @PreAuthorize("hasAuthority('SCOPE_tariff:subscribe')")
    public ResponseEntity<SubscribedResponse> postSubscribe(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        SubscribedResponse result = subscriptionService.subscribe(id, userId);
        // URI вернуть нельзя, т.к. сущность пользователь-подписка недоступна через API
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @DeleteMapping("/unsubscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SCOPE_tariff:subscribe')")
    public void deleteSubscribe(
            @AuthenticationPrincipal Integer userId
    ) {
        subscriptionService.unsubscribe(userId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_tariff:manage')")
    public ResponseEntity<SubscriptionTariffResponse> postSubscription(
            @Valid @RequestBody CreateSubscriptionTariffRequest request
    ) {
        SubscriptionTariffResponse result = subscriptionService
                .addSubscriptionTariff(subscriptionMapper.toCreateCommand(request));

        URI uri = URI.create("/api/subscriptions/" + result.getId());
        return ResponseEntity
                .created(uri)
                .body(result);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_tariff:manage')")
    public ResponseEntity<SubscriptionTariffResponse> patchSubscription(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSubscriptionTariffRequest request
    ) {
        return ResponseEntity.ok(
                subscriptionService.updateSubscription(id, request.getDurationDays())
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_tariff:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubscription(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        subscriptionService.deleteSubscription(id, force);
    }

    // TODO убрать
    @GetMapping("/test")
    public void test() {
        billingService.processBilling();
    }
}
