package com.chump.billing.service;

import com.chump.user.dao.UserSubscriptionDao;
import com.chump.user.model.UserSubscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BillingService {

    private final UserSubscriptionDao userSubscriptionDao;
    private final BillingProcessor billingProcessor;

    public BillingService(UserSubscriptionDao userSubscriptionDao, BillingProcessor billingProcessor) {
        this.userSubscriptionDao = userSubscriptionDao;
        this.billingProcessor = billingProcessor;
    }

    // Облегчаем транзакцию
    // Вместо добавления @Transactional в DAO (требует CGLIB или использования интерфейсов)
    @Transactional(readOnly = true)
    public void processBilling() {
        final int batchSize = 50;
        List<UserSubscription> batch;

        do {
            batch = userSubscriptionDao.findToBillWithUserProfile(LocalDate.now(), batchSize);
            for (UserSubscription sub : batch) {
                try {
                    billingProcessor.processSingleBatch(sub);
                } catch (Exception e) {
                    // TODO логирование
                    throw e;
                }
            }
        } while (batch.size() == batchSize);
    }
}
