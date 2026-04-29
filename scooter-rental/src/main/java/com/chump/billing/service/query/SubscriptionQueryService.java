package com.chump.billing.service.query;

import com.chump.billing.dto.response.CurrentSubscriptionResponse;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.billing.dao.SubscriptionTariffDao;
import com.chump.billing.dao.TariffDao;
import com.chump.billing.dto.response.SubscriptionTariffResponse;
import com.chump.billing.dto.response.TariffConciseResponse;
import com.chump.billing.mapper.SubscriptionMapper;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.model.SubscriptionTariff;
import com.chump.user.dao.UserSubscriptionDao;
import com.chump.user.model.UserSubscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionQueryService {

    private final SubscriptionTariffDao subscriptionTariffDao;
    private final SubscriptionMapper subscriptionMapper;
    private final TariffMapper tariffMapper;
    private final TariffDao tariffDao;
    private final UserSubscriptionDao userSubscriptionDao;

    @Transactional(readOnly = true)
    public List<TariffConciseResponse> getAllSubscriptionTariffs() {
        return tariffMapper.toConciseResponseList(tariffDao.getAllSubscriptionTariffs());
    }

    @Transactional(readOnly = true)
    public SubscriptionTariffResponse getSubscriptionTariff(int subscriptionTariffId) {
        SubscriptionTariff result = subscriptionTariffDao.findByIdWithTariff(subscriptionTariffId).orElseThrow(
                () -> new NoSuchEntityException("No subscription tariff found with id: " + subscriptionTariffId)
        );

        return subscriptionMapper.toSubscriptionResponse(result);
    }

    @Transactional(readOnly = true)
    public CurrentSubscriptionResponse getCurrentSubscriptionOfUser(int userId) {
        UserSubscription subscription = userSubscriptionDao.findById(userId).orElseThrow(
                () -> new NoSuchEntityException("No subscription found for user with id: " + userId)
        );

        SubscriptionTariff tariff = subscriptionTariffDao.findByIdWithTariff(
                subscription.getTariff().getId()
        ).orElseThrow(
                () -> new NoSuchEntityException("No subscription tariff found with id: "
                        + subscription.getTariff().getId())
        );

        return subscriptionMapper.toCurrentSubscriptionResponse(subscription, tariff);
    }
}
