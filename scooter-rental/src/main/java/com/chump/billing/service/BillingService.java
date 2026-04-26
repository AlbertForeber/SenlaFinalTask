package com.chump.billing.service;

import com.chump.billing.dao.BillingBatchFailureDao;
import com.chump.billing.dao.BillingBatchFailureItemDao;
import com.chump.billing.dto.response.ManualBillingFailureResponse;
import com.chump.billing.dto.response.BillingResponse;
import com.chump.billing.model.BillingBatchFailure;
import com.chump.billing.model.BillingBatchFailureItem;
import com.chump.user.dao.UserSubscriptionDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class BillingService {

    private final UserSubscriptionDao userSubscriptionDao;
    private final BillingProcessor billingProcessor;
    private final int batchSize;
    private final BillingBatchFailureDao billingBatchFailureDao;
    private final BillingBatchFailureItemDao billingBatchFailureItemDao;

    public BillingService(UserSubscriptionDao userSubscriptionDao,
                          BillingProcessor billingProcessor,
                          @Value("${billing.batch.size:100}") int batchSize,
                          BillingBatchFailureDao billingBatchFailureDao,
                          BillingBatchFailureItemDao billingBatchFailureItemDao) {
        this.userSubscriptionDao = userSubscriptionDao;
        this.billingProcessor = billingProcessor;
        this.batchSize = batchSize;
        this.billingBatchFailureDao = billingBatchFailureDao;
        this.billingBatchFailureItemDao = billingBatchFailureItemDao;
    }

    // Облегчаем транзакцию
    // Вместо добавления @Transactional в DAO (требует CGLIB или использования интерфейсов)
    @Transactional
    public BillingResponse processBilling() {
        int page = 0;
        int failed = 0;
        int successful = 0;
        Instant start = Instant.now();
        List<Integer> batch = userSubscriptionDao.batchFindToBillIds(batchSize, page);

        while (!batch.isEmpty()) {
            try {
                billingProcessor.processSingleBatch(batch);
                successful += batch.size();
            } catch (Exception e) {
                log.error("Batch billing failed for ids: {} - {}.", batch.get(0), batch.get(batch.size() - 1), e);
                failed += batch.size();
                saveFailure(e, batch);
            } finally {
                page++;
                batch = userSubscriptionDao.batchFindToBillIds(batchSize, page);
            }
        }

        log.info("Billing completed in {} ms. Successful: {}, failed: {}.",
                Duration.between(start, Instant.now()).toMillis(),
                successful,
                failed
        );

        return BillingResponse.builder()
                .totalSuccess(successful)
                .totalFailed(failed)
                .build();
    }

    // ------- Ручной биллинг имеет два режима -------
    // 1) failedOnly - построчно проходится по всем неудачным списанием и собирает ошибки
    //    для каждого неудачного списания, а не для батча целиком.
    // 2) !failedOnly - просто ручной запуск processBilling.
    @Transactional
    public BillingResponse manualBilling(boolean failedOnly) {
        if (!failedOnly) {
            return processBilling();
        }

        int page = 0;
        int failed = 0;
        int successful = 0;
        List<ManualBillingFailureResponse> details = new ArrayList<>();

        List<BillingBatchFailureItem> batch = billingBatchFailureItemDao.batchFindAll(batchSize, page);
        while (!batch.isEmpty()) {
            for (BillingBatchFailureItem item : batch) {
                try {
                    billingProcessor.processSingleBatch(
                            Collections.singletonList(item.getId().getUserSubscriptionId())
                    );
                    successful++;
                } catch (Exception e) {
                    failed++;
                    details.add(ManualBillingFailureResponse.builder()
                            .subscriptionId(item.getId().getUserSubscriptionId())
                            .errorMessage(e.getCause().getMessage()) // getCause() т.к. все ошибки Dao оборачиваются
                            .build());                                       // в кастомное исключение, а здесь нужна сама проблема
                } finally {
                    page++;
                    batch = billingBatchFailureItemDao.batchFindAll(batchSize, page);
                }
            }
        }

        return BillingResponse.builder()
                .failedDetails(details)
                .totalSuccess(successful)
                .totalFailed(failed)
                .build();
    }

    private void saveFailure(Exception e, List<Integer> batch) {
        try {
            BillingBatchFailure failure = billingBatchFailureDao.save(BillingBatchFailure.builder()
                    .failedAt(Instant.now())
                    .errorMessage(e.getCause().getMessage()) // getCause, т.к. все ошибки Dao оборачиваются в
                    .isResolved(false)                       // кастомное исключение, а здесь нужно получить саму проблему
                    .build());

            billingBatchFailureItemDao.batchSave(failure.getId(), batch);
        } catch (Exception ex) {
            log.error("Failed to save batch failure for ids: {} - {}", batch.get(0), batch.get(batch.size() - 1), ex);
        }
    }
}
