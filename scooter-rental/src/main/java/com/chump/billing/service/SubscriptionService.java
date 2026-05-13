package com.chump.billing.service;

import com.chump.common.exception.NoRequiredEntityException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.billing.dao.SubscriptionTariffDao;
import com.chump.billing.dao.TariffDao;
import com.chump.billing.dto.command.CreateSubscriptionTariffCommand;
import com.chump.billing.dto.response.SubscribedResponse;
import com.chump.billing.dto.response.SubscriptionTariffResponse;
import com.chump.billing.mapper.SubscriptionMapper;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.model.SubscriptionTariff;
import com.chump.billing.model.Tariff;
import com.chump.common.utils.TransactionUtils;
import com.chump.rental.dao.TripDao;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.dao.UserSubscriptionDao;
import com.chump.user.model.UserProfile;
import com.chump.user.model.UserSubscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final UserSubscriptionDao userSubscriptionDao;
    private final SubscriptionTariffDao subscriptionTariffDao;
    private final TariffDao tariffDao;
    private final UserProfileDao userProfileDao;
    private final TripDao tripDao;
    private final SubscriptionMapper subscriptionMapper;
    private final TariffMapper tariffMapper;
    private final TransactionUtils transactionUtils;

    @Transactional
    public SubscribedResponse subscribe(int tariffId, int userId) {
        if (userSubscriptionDao.findById(userId).isPresent()) {
            throw new UnavaliableActionException(
                    "Unable to subscribe when there is an active subscription. Please unsubscribe first"
            );
        }

        if (!tripDao.findOngoingByUserId(userId).isEmpty()) {
            throw new UnavaliableActionException(
                    "Unable change subscription with ongoing trips. Please finish them first"
            );
        }

        SubscriptionTariff subscriptionTariff = subscriptionTariffDao.findByIdWithTariff(tariffId).orElseThrow(
                () -> new NoSuchEntityException(
                        "No subscription tariff found with id: " + tariffId
                                + ". Ensure the tariff you are trying to subscribe is subscription"
                )
        );
        UserProfile profile = userProfileDao.findById(userId).orElseThrow(
                () -> new NoRequiredEntityException("No user found with id: " + userId)
        );

        if (profile.getBalance().compareTo(subscriptionTariff.getTariff().getBasePrice()) < 0) {
            throw new UnavaliableActionException("Not enough money to subscribe");
        }

        UserSubscription userSubscription = UserSubscription.builder()
                .id(profile.getId())
                .userProfile(profile)
                .tariff(subscriptionTariff.getTariff())
                .nextBillingDate(LocalDate.now().plusDays(subscriptionTariff.getDurationDays()))
                .build();

        profile.setBalance(profile.getBalance().subtract(subscriptionTariff.getTariff().getBasePrice()));
        userSubscriptionDao.save(userSubscription);

        transactionUtils.afterCommit(() ->
                log.info("Successfully subscribed for user with id: {} to tariff with id: {}", userId, tariffId)
        );

        return subscriptionMapper.toSubscribedResponse(userSubscription);
    }

    @Transactional
    public void unsubscribe(int userId) {
        if (!tripDao.findOngoingByUserId(userId).isEmpty()) {
            throw new UnavaliableActionException(
                    "Unable change subscription with ongoing trips. Please finish them first"
            );
        }

        userSubscriptionDao.delete(userId);

        log.info("Successfully unsubscribed for user with id: {}", userId);
    }

    @Transactional
    public SubscriptionTariffResponse updateSubscription(int tariffId, int durationDays) {
        SubscriptionTariff subscriptionTariff = subscriptionTariffDao.findByIdWithTariff(tariffId).orElseThrow(
                () -> new NoSuchEntityException(
                        "No subscription tariff found with id: " + tariffId
                )
        );

        subscriptionTariff.setDurationDays(durationDays);

        transactionUtils.afterCommit(() ->
                log.info(
                        "Successfully updated duration days to {} for subscription with id: {}", durationDays, tariffId
                )
        );

        return subscriptionMapper.toSubscriptionResponse(subscriptionTariff);
    }

    @Transactional
    public SubscriptionTariffResponse addSubscriptionTariff(CreateSubscriptionTariffCommand command) {
        Tariff tariff = tariffMapper.toEntityForSubscription(command);
        tariff = tariffDao.save(tariff); // CascadeType.PERSIST не поможет, т.к. сначала нужно сохранить тариф

        SubscriptionTariff subscriptionTariff = subscriptionMapper.toEntity(command, tariff);

        transactionUtils.afterCommit(() ->
                log.info("Successfully added subscription tariff with id: {}", subscriptionTariff.getId())
        );

        return subscriptionMapper.toSubscriptionResponse(subscriptionTariffDao.save(subscriptionTariff));
    }

    @Transactional
    public void deleteSubscription(int tariffId, boolean isForce) {
        SubscriptionTariff subscriptionTariff = subscriptionTariffDao.findByIdWithTariff(tariffId).orElseThrow(
                () -> new NoSuchEntityException("No subscription tariff found with id: " + tariffId)
        );

        List<UserSubscription> subscriptions = userSubscriptionDao.findByTariffIdWithUserProfile(tariffId);

        if (!subscriptions.isEmpty()) {
            if (!isForce) {
                throw new UnavaliableActionException("There're active subscriptions for this tariff. " +
                        "Use 'force=true' to refund before deleting");
            }

            refund(subscriptions, subscriptionTariff);
        }

        tariffDao.delete(tariffId);
        log.info("Successfully deleted subscription tariff with id: {}", tariffId);
    }

    private void refund(List<UserSubscription> subscriptions, SubscriptionTariff subscriptionTariff) {
        final LocalDate now = LocalDate.now();
        final BigDecimal basePrice = subscriptionTariff.getTariff().getBasePrice();
        final Integer durationDays = subscriptionTariff.getDurationDays();

        int refunded = 0;

        for (UserSubscription subscription : subscriptions) {
            UserProfile subscriber = subscription.getUserProfile();
            LocalDate expiredAt = subscription.getNextBillingDate();
            BigDecimal toAdd = basePrice.multiply(BigDecimal.valueOf(
                            ChronoUnit.DAYS.between(now, expiredAt)
                    )).divide(BigDecimal.valueOf(durationDays), 2, RoundingMode.HALF_UP);

            subscriber.setBalance(subscriber.getBalance().add(toAdd));

            refunded++;
        }

        log.info("{} users have been refunded after subscription deletion", refunded);
    }
}