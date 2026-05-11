package com.chump.billing.service.query;

import com.chump.billing.dao.SubscriptionTariffDao;
import com.chump.billing.dao.TariffDao;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.model.Tariff;
import com.chump.billing.service.query.SubscriptionQueryService;
import com.chump.common.exception.NoRequiredEntityException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.user.dao.UserSubscriptionDao;
import com.chump.user.model.UserSubscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Subscription query service testing")
public class SubscriptionQueryServiceTest {

    @Mock
    private SubscriptionTariffDao subscriptionTariffDao;
    @Mock
    private UserSubscriptionDao userSubscriptionDao;
    @Mock
    private TariffDao tariffDao;
    @Mock
    private TariffMapper tariffMapper;

    @InjectMocks
    private SubscriptionQueryService service;

    @Test
    @Tag("unit")
    @DisplayName("Get subscription throw an exception, if unknown subscription ID")
    public void getSubscriptionShouldThrowWhenUnknown() {
        when(subscriptionTariffDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.getSubscriptionTariff(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown subscription ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Get current subscription of user should throw an exception, if user doesn't have subscription")
    public void getCurrentSubscriptionShouldThrowWhenNoSubscription() {
        when(userSubscriptionDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.getCurrentSubscriptionOfUser(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain not found subscription ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Get current subscription of user should throw an exception, if subscription tariff not found")
    public void getCurrentSubscriptionShouldThrowWhenNoTariff() {
        when(userSubscriptionDao.findById(anyInt())).thenReturn(Optional.of(
                UserSubscription.builder()
                        .tariff(new Tariff(1, null, null, null))
                        .build()
        ));
        when(subscriptionTariffDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());

        NoRequiredEntityException exception = assertThrows(NoRequiredEntityException.class,
                () -> service.getCurrentSubscriptionOfUser(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain not found subscription ID");
    }
}