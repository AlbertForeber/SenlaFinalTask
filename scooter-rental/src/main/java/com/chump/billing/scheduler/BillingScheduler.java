package com.chump.billing.scheduler;

import com.chump.billing.service.BillingService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillingScheduler {

    private final BillingService billingService;

    public BillingScheduler(BillingService billingService) {
        this.billingService = billingService;
    }

    // TODO поставить ежедневно раз в 3 часа ночи
    @Scheduled(initialDelay = 5000L, fixedDelay = 30000L)
    @SchedulerLock(name = "billing_daily", lockAtLeastFor = "PT1M", lockAtMostFor = "PT1H")
    public void runDailyBilling() {
        billingService.processBilling();
    }
}
