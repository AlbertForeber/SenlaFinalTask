package com.chump.billing.service;

import com.chump.billing.dao.SubscriptionTariffDao;
import com.chump.billing.dao.TariffDao;
import com.chump.billing.mapper.SubscriptionMapper;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.model.SubscriptionTariff;
import com.chump.billing.model.Tariff;
import com.chump.common.exception.NoRequiredEntityException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavailableActionException;
import com.chump.common.utils.TransactionUtils;
import com.chump.rental.dao.TripDao;
import com.chump.rental.model.Trip;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.dao.UserSubscriptionDao;
import com.chump.user.model.UserProfile;
import com.chump.user.model.UserSubscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Subscription service testing")
public class SubscriptionServiceTest {

    @Mock private UserSubscriptionDao userSubscriptionDao;
    @Mock private SubscriptionTariffDao subscriptionTariffDao;
    @Mock private TariffDao tariffDao;
    @Mock private UserProfileDao userProfileDao;
    @Mock private TripDao tripDao;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private TariffMapper tariffMapper;
    @Mock private TransactionUtils transactionUtils;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    @Tag("unit")
    @DisplayName("Subscribe method should throw exception, if user already has a subscription")
    public void subscribeShouldThrowWhenActiveSubscription() {
        when(userSubscriptionDao.findById(anyInt())).thenReturn(Optional.of(UserSubscription.builder().build()));
        assertThrows(UnavailableActionException.class, () -> subscriptionService.subscribe(1, 1));

    }

    @Test
    @Tag("unit")
    @DisplayName("Subscribe method should throw exception, if user has ongoing trips")
    public void subscribeShouldThrowWhenOngoingTrips() {
        when(userSubscriptionDao.findById(anyInt())).thenReturn(Optional.empty());
        when(tripDao.findOngoingByUserId(anyInt())).thenReturn(Collections.singletonList(
                Trip.builder().build()
        ));

        assertThrows(UnavailableActionException.class,
                () -> subscriptionService.subscribe(1, 1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Subscribe method should throw exception, if subscription tariff ID is unknown")
    public void subscribeShouldThrowWhenUnknownTariff() {
        when(userSubscriptionDao.findById(anyInt())).thenReturn(Optional.empty());
        when(tripDao.findOngoingByUserId(anyInt())).thenReturn(Collections.emptyList());
        when(subscriptionTariffDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> subscriptionService.subscribe(1, 1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown subscription tariff ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Subscribe method should throw exception, if user profile not found")
    public void subscribeShouldThrowWhenNoUser() {
        when(userSubscriptionDao.findById(anyInt())).thenReturn(Optional.empty());
        when(tripDao.findOngoingByUserId(anyInt())).thenReturn(Collections.emptyList());
        when(subscriptionTariffDao.findByIdWithTariff(anyInt())).thenReturn(Optional.of(
                new SubscriptionTariff(1, null, 1)
        ));
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.empty());

        NoRequiredEntityException exception = assertThrows(NoRequiredEntityException.class,
                () -> subscriptionService.subscribe(1, 1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown user ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Subscribe method should throw exception, if not enough money")
    public void subscribeShouldThrowWhenNotEnoughMoney() {
        when(userSubscriptionDao.findById(anyInt())).thenReturn(Optional.empty());
        when(tripDao.findOngoingByUserId(anyInt())).thenReturn(Collections.emptyList());
        when(subscriptionTariffDao.findByIdWithTariff(anyInt())).thenReturn(Optional.of(
                new SubscriptionTariff(
                        1,
                        new Tariff(
                        1, null, BigDecimal.ONE, null),
                        1
                )
        ));
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(
                new UserProfile(1, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO)
        ));

        assertThrows(UnavailableActionException.class,
                () -> subscriptionService.subscribe(1, 1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Unsubscribe method should throw exception, if user has ongoing trips")
    public void unsubscribeShouldThrowWhenOngoingTrips() {
        when(tripDao.findOngoingByUserId(anyInt())).thenReturn(Collections.singletonList(
                Trip.builder().build()
        ));

        assertThrows(UnavailableActionException.class,
                () -> subscriptionService.unsubscribe(1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Update subscription method should update subscription duration days")
    public void updateSubscriptionShouldUpdate() {
        SubscriptionTariff tariff = new SubscriptionTariff(1, null, 1);

        when(subscriptionTariffDao.findByIdWithTariff(anyInt())).thenReturn(Optional.of(
                tariff
        ));

        subscriptionService.updateSubscription(1, 2);
        assertEquals(2, tariff.getDurationDays(),
                "Subscription duration should be updated");
    }

    @Test
    @Tag("unit")
    @DisplayName("Update subscription method should throw, if unknown subscription ID")
    public void updateSubscriptionShouldThrowWhenUnknownSubscription() {
        when(subscriptionTariffDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> subscriptionService.updateSubscription(1, 1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown subscription ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Delete subscription method should throw, if unknown subscription ID")
    public void deleteSubscriptionShouldThrowWhenUnknownSubscription() {
        when(subscriptionTariffDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> subscriptionService.deleteSubscription(1, false));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown subscription ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Delete subscription method should refund, when there're active subscribers and forced")
    public void deleteSubscriptionShouldRefundWhenActiveSubscribersForce() {
        UserProfile userProfile = new UserProfile(
                1,
                null,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(subscriptionTariffDao.findByIdWithTariff(anyInt())).thenReturn(Optional.of(
                new SubscriptionTariff(
                        1,
                        new Tariff(
                                1, null, BigDecimal.valueOf(30), null),
                        30
                )
        ));
        when(userSubscriptionDao.findByTariffIdWithUserProfile(anyInt())).thenReturn(Collections.singletonList(
                UserSubscription.builder()
                        .nextBillingDate(LocalDate.now().plusDays(1))
                        .userProfile(userProfile)
                        .build()
        ));

        subscriptionService.deleteSubscription(1, true);
        assertEquals(0, BigDecimal.ONE.compareTo(userProfile.getBalance()),
                "Remaining days should be refunded");
    }
}
