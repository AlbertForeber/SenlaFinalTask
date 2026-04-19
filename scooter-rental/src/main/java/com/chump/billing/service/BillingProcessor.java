package com.chump.billing.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.billing.dao.TariffDao;
import com.chump.billing.dto.BillingData;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.dao.UserSubscriptionDao;
import com.chump.user.model.UserProfile;
import com.chump.user.model.UserSubscription;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BillingProcessor {

    private final TariffDao tariffDao;
    private final UserSubscriptionDao userSubscriptionDao;
    private final UserProfileDao userProfileDao;

    public BillingProcessor(TariffDao tariffDao, UserSubscriptionDao userSubscriptionDao, UserProfileDao userProfileDao) {
        this.tariffDao = tariffDao;
        this.userSubscriptionDao = userSubscriptionDao;
        this.userProfileDao = userProfileDao;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleBatch(UserSubscription userSubscription) {
        UserProfile userProfile = userSubscription.getUserProfile();
        BillingData billingData = tariffDao.getBillingData(userSubscription.getTariff().getId()).orElseThrow(
                () -> new NoSuchEntityException("No subscription tariff found with id: "
                        + userSubscription.getTariff().getId())
        );

        if (userProfile.getBalance().compareTo(billingData.getBasePrice()) < 0) {
            userSubscriptionDao.delete(userSubscription.getId()); // TODO просто переводим пользователя на обычный тариф
        }

        userProfile.setBalance(userProfile.getBalance().subtract(billingData.getBasePrice()));
        userSubscription.setNextBillingDate(
                userSubscription.getNextBillingDate().plusDays(billingData.getDurationDays())
        );

        // Dirty Check не происходит, т.к. мы создаем новую транзакцию
        // А сюда попадает Detached объект => ручной update (merge).
        userProfileDao.update(userProfile);
        userSubscriptionDao.update(userSubscription);
    }
}
