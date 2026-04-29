package com.chump.billing.service;

import com.chump.user.dao.UserSubscriptionDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.lang.Thread.sleep;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingProcessor {

    private final UserSubscriptionDao userSubscriptionDao;

    @SuppressWarnings("BusyWait")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleBatch(List<Integer> batchIds) throws InterruptedException {
//        UserProfile userProfile = userSubscription.getUserProfile();
//        BillingData billingData = tariffDao.getBillingData(userSubscription.getTariff().getId()).orElseThrow(
//                () -> new NoSuchEntityException("No subscription tariff found with id: "
//                        + userSubscription.getTariff().getId())
//        );
//
//        if (userProfile.getBalance().compareTo(billingData.getBasePrice()) < 0) {
//            userSubscriptionDao.delete(userSubscription.getId()); // TODO просто переводим пользователя на обычный тариф
//        }
//
//        userProfile.setBalance(userProfile.getBalance().subtract(billingData.getBasePrice()));
//        userSubscription.setNextBillingDate(
//                userSubscription.getNextBillingDate().plusDays(billingData.getDurationDays())
//        );
//
//        // Dirty Check не происходит, т.к. мы создаем новую транзакцию
//        // А сюда попадает Detached объект => ручной update (merge).
//        userProfileDao.update(userProfile);
//        userSubscriptionDao.update(userSubscription);
        int attempts = 0;
        int delay = 500;

        while (true) {
            try {
                userSubscriptionDao.batchDeleteUnableToPay(batchIds);
                userSubscriptionDao.batchProcessBilling(batchIds);
                userSubscriptionDao.batchUpdateLastBillingDate(batchIds);
                return;
            } catch (Exception e) {
                if (attempts == 3 || !isRetryable(e)) throw e;
                log.warn("Billing batch processing failed. Retry {}/3", attempts);
                sleep(delay);

                attempts++;
                delay *= 2;
            }
        }
    }

    private boolean isRetryable(Exception e) {
        // Пробуем еще раз, если deadlock / Ошибка связи к БД
        return e instanceof LockAcquisitionException || e instanceof JDBCConnectionException;
    }
}
