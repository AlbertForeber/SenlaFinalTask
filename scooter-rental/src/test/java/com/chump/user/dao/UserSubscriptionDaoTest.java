package com.chump.user.dao;

import com.chump.billing.model.Tariff;
import com.chump.common.dao.AbstractDaoTest;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import com.chump.user.model.UserSubscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User subscription DAO testing")
@ContextConfiguration(classes = UserSubscriptionDao.class)
public class UserSubscriptionDaoTest extends AbstractDaoTest {

    @Autowired
    private UserSubscriptionDao userSubscriptionDao;

    @Test
    @DisplayName("find by ID with tariff should return user subscription")
    public void findByIdWithTariffShouldReturnUserSubscription() {
        UserSubscription userSubscription = new UserSubscription(
                1,
                new UserProfile(
                        1,
                        new User(
                                1,
                                null,
                                null,
                                null
                        ),
                        null,
                        null,
                        null,
                        null
                ),
                new Tariff( // Тариф с ежемесячной оплатой
                        3,
                        null,
                        null,
                        null
                ),
                LocalDate.now().plusDays(30)
        );

        userSubscriptionDao.save(userSubscription);
        flushAndClear();

        Optional<UserSubscription> foundSubscription = userSubscriptionDao.findByIdWithTariff(1);
        assertTrue(foundSubscription.isPresent());
        assertAll("User subscription fields check",
                () -> assertEquals(1, foundSubscription.get().getId()),
                () -> assertEquals(3, foundSubscription.get().getTariff().getId()),
                () -> assertNull(foundSubscription.get().getTariff().getBillingIntervalMinutes())
        );
    }
}
