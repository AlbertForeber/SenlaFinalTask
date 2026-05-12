package com.chump.rental.service;

import com.chump.billing.dao.TariffDao;
import com.chump.billing.model.Tariff;
import com.chump.common.exception.NoRequiredEntityException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.common.utils.TransactionUtils;
import com.chump.rental.dao.*;
import com.chump.rental.kafka.ScooterProducer;
import com.chump.rental.mapper.TripMapper;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.Trip;
import com.chump.rental.model.status.ScooterStatus;
import com.chump.rental.model.status.TripStatus;
import com.chump.rental.repo.ScooterRepository;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.dao.UserSubscriptionDao;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import com.chump.user.model.UserSubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Rental service testing")
@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {

    @Mock private ScooterRepository scooterRepository;
    @Mock private TripDao tripDao;
    @Mock private TripMapper tripMapper;
    @Mock private UserProfileDao userProfileDao;
    @Mock private UserSubscriptionDao userSubscriptionDao;
    @Mock private TariffDao tariffDao;
    @Mock private TripPointDao tripPointDao;
    @Mock private RentalSpotDao rentalSpotDao;
    @Mock private ScooterProducer scooterProducer;
    @Mock private ScooterPendingRedisDao scooterPendingRedisDao;
    @Mock private ScooterWaypointRedisDao scooterWaypointRedisDao;
    @Mock private TripTimeLimitRedisDao tripTimeLimitRedisDao;
    @Mock private TransactionUtils transactionUtils;

    @InjectMocks
    private RentalService service;

    @BeforeEach
    public void init() {
        ReflectionTestUtils.setField(service, "minToStart", 10);
    }

    @Test
    @Tag("unit")
    @DisplayName("Rent scooter method should throw an exception, if scooter ID is unknown")
    public void rentScooterShouldThrowWhenUnknownScooterId() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.rentScooter(1, 1, 1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown scooter ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Rent scooter method should throw an exception, if is not free")
    public void rentScooterShouldThrowWhenNotFreeScooter() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.OCCUPIED
                )
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.rentScooter(1, 1, 1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Rent scooter method should throw an exception, if unknown user ID")
    public void rentScooterShouldThrowWhenUnknownUserId() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.FREE
                )
        ));

        when(userProfileDao.findById(anyInt())).thenReturn(Optional.empty());
        NoRequiredEntityException exception = assertThrows(NoRequiredEntityException.class,
                () -> service.rentScooter(1, 1, 1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown user ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Rent scooter method should throw an exception, if user trying to choose tariff with active subscription")
    public void rentScooterShouldThrowWhenChangeTariffWithActiveSubscription() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.FREE
                )
        ));

        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(
                new UserProfile(
                        1,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        ));

        when(userSubscriptionDao.findByIdWithTariff(anyInt())).thenReturn(Optional.of(
                UserSubscription.builder().build()
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.rentScooter(1, 1, 1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Rent scooter method should throw an exception, if tariff ID is unknown")
    public void rentScooterShouldThrowWhenUnknownTariff() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.FREE
                )
        ));

        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(
                new UserProfile(
                        1,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        ));

        when(userSubscriptionDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());
        when(tariffDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.rentScooter(1, 1, 1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown tariff ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Rent scooter method should throw an exception, if default tariff was not found")
    public void rentScooterShouldThrowWhenNoDefaultTariff() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.FREE
                )
        ));

        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(
                new UserProfile(
                        1,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        ));

        when(userSubscriptionDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());
        when(tariffDao.findDefaultTariff()).thenReturn(Optional.empty());

        assertThrows(NoRequiredEntityException.class,
                () -> service.rentScooter(1, 1, 0));
    }

    @Test
    @Tag("unit")
    @DisplayName("Rent scooter method should throw an exception, if trying to choose subscription tariff")
    public void rentScooterShouldThrowWhenSubscriptionTariff() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.FREE
                )
        ));

        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(
                new UserProfile(
                        1,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        ));

        when(userSubscriptionDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());
        when(tariffDao.findById(anyInt())).thenReturn(Optional.of(
                new Tariff(
                        1,
                        null,
                        null,
                        null
                )
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.rentScooter(1, 1, 1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Rent scooter method should throw an exception, if tariff is interval and insufficient balance")
    public void rentScooterShouldThrowWhenInsufficientBalance() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.FREE
                )
        ));

        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(
                new UserProfile(
                        1,
                        null,
                        null,
                        null,
                        BigDecimal.ZERO,
                        null
                )
        ));

        when(userSubscriptionDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());
        when(tariffDao.findById(anyInt())).thenReturn(Optional.of(
                new Tariff(
                        1,
                        null,
                        BigDecimal.ONE,
                        1
                )
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.rentScooter(1, 1, 1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Rent scooter method should throw an exception, if tariff is interval with price higher than min to start and insufficient balance w")
    public void rentScooterShouldThrowWhenInsufficientBalanceAndMinToStartLower() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.FREE
                )
        ));

        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(
                new UserProfile(
                        1,
                        null,
                        null,
                        null,
                        BigDecimal.valueOf(10),
                        null
                )
        ));

        when(userSubscriptionDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());
        when(tariffDao.findById(anyInt())).thenReturn(Optional.of(
                new Tariff(
                        1,
                        null,
                        BigDecimal.valueOf(11),
                        1
                )
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.rentScooter(1, 1, 1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Rent scooter method should unlock scooter and set time limit")
    public void rentScooterShouldUnlockScooterWithTimeLimit() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.FREE
                )
        ));

        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(
                new UserProfile(
                        1,
                        null,
                        null,
                        null,
                        BigDecimal.valueOf(10),
                        null
                )
        ));

        when(userSubscriptionDao.findByIdWithTariff(anyInt())).thenReturn(Optional.empty());
        when(tariffDao.findById(anyInt())).thenReturn(Optional.of(
                new Tariff(
                        1,
                        null,
                        BigDecimal.ONE,
                        1
                )
        ));
        when(tripDao.save(any())).thenAnswer(invocationOnMock -> {
            Trip trip = invocationOnMock.getArgument(0);
            trip.setId(1);
            return trip;
        });
        doAnswer(invocationOnMock -> {
            Runnable action = invocationOnMock.getArgument(0);
            action.run();
            return null;
        }).when(transactionUtils).afterCommit(any());

        assertDoesNotThrow(() -> service.rentScooter(1, 1, 1));

        verify(scooterPendingRedisDao, only()).setPending(1);
        verify(scooterProducer, only()).sendUnlock(1);
        verify(tripTimeLimitRedisDao, only()).setTimeLimit(anyInt(), eq(630));
    }

    @Test
    @Tag("unit")
    @DisplayName("Pause scooter method should throw an exception, if no trip for given scooter ID")
    public void pauseScooterShouldThrowWhenNoTripScooterId() {
        when(tripDao.findOngoingByScooterId(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.pauseScooter(1, 1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain given scooter ID with no trips");
    }

    @Test
    @Tag("unit")
    @DisplayName("Pause scooter method should throw an exception, if it's other user's trip")
    public void pauseScooterShouldThrowWhenSomeoneElseTrip() {
        when(tripDao.findOngoingByScooterId(anyInt())).thenReturn(Optional.of(
                Trip.builder().user(
                        User.builder().id(2).build()
                ).build()
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.pauseScooter(1, 1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Resume scooter method should throw an exception, if no trip for given scooter ID")
    public void resumeScooterShouldThrowWhenNoTripScooterId() {
        when(tripDao.findOngoingByScooterId(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.resumeScooter(1, 1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain given scooter ID with no trips");
    }

    @Test
    @Tag("unit")
    @DisplayName("Resume scooter method should throw an exception, if it's other user's trip")
    public void resumeScooterShouldThrowWhenSomeoneElseTrip() {
        when(tripDao.findOngoingByScooterId(anyInt())).thenReturn(Optional.of(
                Trip.builder().user(
                        User.builder().id(2).build()
                ).build()
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.resumeScooter(1, 1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Return scooter method should throw an exception, if no trip for given scooter ID")
    public void returnScooterShouldThrowWhenNoTripScooterId() {
        when(tripDao.findOngoingByScooterId(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.returnScooter(1, 1, false));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain given scooter ID with no trips");
    }

    @Test
    @Tag("unit")
    @DisplayName("Return scooter method should throw an exception, if scooter is not in parking rental spot and is not force")
    public void returnScooterShouldThrowWhenNotInParkingZone() {
        when(tripDao.findOngoingByScooterId(anyInt())).thenReturn(Optional.of(
                Trip.builder()
                        .user(
                            User.builder().id(1).build())
                        .scooter(
                                new Scooter(1,null, null, null, null, null))
                        .build()
        ));
        when(rentalSpotDao.isInParkingRentalSpot(any())).thenReturn(false);

        assertThrows(UnavaliableActionException.class,
                () -> service.returnScooter(1, 1, false));
    }

    @Test
    @Tag("unit")
    @DisplayName("Return scooter method should throw an exception, if scooter is not in parking rental spot and is not force")
    public void returnScooterShouldThrowWhenUserProfileNotFound() {
        when(tripDao.findOngoingByScooterId(anyInt())).thenReturn(Optional.of(
                Trip.builder()
                        .user(
                                User.builder().id(1).build())
                        .scooter(
                                new Scooter(1,null, null, null, null, null))
                        .build()
        ));
        when(rentalSpotDao.isInParkingRentalSpot(any())).thenReturn(false);
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.empty());

        NoRequiredEntityException exception = assertThrows(NoRequiredEntityException.class,
                () -> service.returnScooter(1, 1, true));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain not found user profile ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Return scooter method should update user, trip and scooter")
    public void returnScooterShouldUpdateScooterTripAndUser() {
        Scooter scooter = new Scooter(1,null, null, null, null, null);
        Trip trip = Trip.builder()
                .id(1)
                .user(
                        User.builder().id(1).build())
                .scooter(scooter)
                .priceAtStart(BigDecimal.ONE)
                .intervalAtStart(1)
                .discountAtStart(BigDecimal.ZERO)
                .startedAt(Instant.now().minusMillis(600000))
                .durationSeconds(600)
                .build();
        UserProfile userProfile = new UserProfile(
                1,
                null,
                null,
                null,
                BigDecimal.TEN,
                BigDecimal.ZERO
        );

        when(tripDao.findOngoingByScooterId(anyInt())).thenReturn(Optional.of(trip));
        when(rentalSpotDao.isInParkingRentalSpot(any())).thenReturn(true);
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(userProfile));
        doAnswer(invocationOnMock -> {
            Runnable action = invocationOnMock.getArgument(0);
            action.run();
            return null;
        }).when(transactionUtils).afterCommit(any());

        service.returnScooter(1, 1, false);
        assertEquals(TripStatus.FINISHED, trip.getStatus());
        assertEquals(ScooterStatus.STOPPING, scooter.getStatus());
        assertEquals(0, userProfile.getBalance().compareTo(BigDecimal.ZERO));

        verify(scooterPendingRedisDao, only()).setPending(anyInt());
        verify(tripTimeLimitRedisDao, only()).deleteTimeLimit(anyInt());
        verify(scooterProducer, only()).sendLock(anyInt());
    }
}
