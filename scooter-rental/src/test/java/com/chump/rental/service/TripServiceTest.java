package com.chump.rental.service;
import com.chump.common.exception.NoRequiredEntityException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.common.utils.TransactionUtils;
import com.chump.notification.service.EmailService;
import com.chump.rental.dao.TripDao;
import com.chump.rental.model.Trip;
import com.chump.rental.model.status.TripStatus;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Trip service testing")
@ExtendWith(MockitoExtension.class)
public class TripServiceTest {

    @Mock private TripDao tripDao;
    @Mock private UserProfileDao userProfileDao;
    @Mock private EmailService emailService;
    @Mock private TransactionUtils transactionUtils;

    @InjectMocks
    private TripService service;

    @Test
    @Tag("unit")
    @DisplayName("Refund trip method should throw an exception, if trip ID is unknown")
    public void refundTripShouldThrowWhenUnknownTripId() {
        when(tripDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.refundTrip(1, 0));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown trip ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Refund trip method should throw an exception, if trip status is ONGOING")
    public void refundTripShouldThrowWhenOngoing() {
        when(tripDao.findById(anyInt())).thenReturn(Optional.of(
                Trip.builder()
                        .status(TripStatus.ONGOING)
                        .build()
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.refundTrip(1, 0));
    }

    @Test
    @Tag("unit")
    @DisplayName("Refund trip method should throw an exception, if trip is already fully refunded")
    public void refundTripShouldThrowWhenAlreadyRefunded() {
        when(tripDao.findById(anyInt())).thenReturn(Optional.of(
                Trip.builder()
                        .status(TripStatus.ONGOING)
                        .totalPrice(BigDecimal.ZERO)
                        .build()
        ));

        UnavaliableActionException exception = assertThrows(UnavaliableActionException.class,
                () -> service.refundTrip(1, 0));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain refunded trip ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Refund trip method should throw an exception, if trip's user profile not found")
    public void refundTripShouldThrowWhenNotFoundUserProfile() {
        when(tripDao.findById(anyInt())).thenReturn(Optional.of(
                Trip.builder()
                        .status(TripStatus.FINISHED)
                        .totalPrice(BigDecimal.ONE)
                        .user(User.builder().id(1).build())
                        .build()
        ));
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.empty());

        NoRequiredEntityException exception = assertThrows(NoRequiredEntityException.class,
                () -> service.refundTrip(1, 0));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain not found user profile ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Refund trip method should fully refund, if 'refundForLastSeconds' is not given")
    public void refundTripShouldRefundWhenNoRefundForLastSeconds() {
        Trip trip = Trip.builder()
                .status(TripStatus.FINISHED)
                .totalPrice(BigDecimal.ONE)
                .user(User.builder().id(1).build())
                .build();
        UserProfile userProfile = new UserProfile(
                1,
                null,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        when(tripDao.findById(anyInt())).thenReturn(Optional.of(trip));
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(userProfile));

        service.refundTrip(1, 0);

        assertEquals(0, userProfile.getBalance().compareTo(BigDecimal.ONE));
    }

    @Test
    @Tag("unit")
    @DisplayName("Refund trip method should fully refund, if 'refundForLastSeconds' is greater or equal trip duration")
    public void refundTripShouldRefundWhenRefundForLastSecondsGeTripDuration() {
        Trip trip = Trip.builder()
                .status(TripStatus.FINISHED)
                .totalPrice(BigDecimal.ONE)
                .durationSeconds(9)
                .user(User.builder().id(1).build())
                .build();
        UserProfile userProfile = new UserProfile(
                1,
                null,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        when(tripDao.findById(anyInt())).thenReturn(Optional.of(trip));
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(userProfile));

        service.refundTrip(1, 10);

        assertEquals(0, userProfile.getBalance().compareTo(BigDecimal.ONE));
    }

    @Test
    @Tag("unit")
    @DisplayName("Refund trip method should refund, update duration and send mail, if 'refundForLastSeconds' is less than trip duration")
    public void refundTripShouldRefundUpdateDurationSendMailWhenRefundForLastSeconds() {
        Trip trip = Trip.builder()
                .status(TripStatus.FINISHED)
                .totalPrice(BigDecimal.valueOf(2))
                .priceAtStart(BigDecimal.ONE)
                .discountAtStart(BigDecimal.ZERO)
                .intervalAtStart(1)
                .durationSeconds(120)
                .user(User.builder().id(1).build())
                .build();
        UserProfile userProfile = new UserProfile(
                1,
                null,
                "test@example.com",
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        when(tripDao.findById(anyInt())).thenReturn(Optional.of(trip));
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(userProfile));
        doAnswer(invocationOnMock -> {
            Runnable action = invocationOnMock.getArgument(0);
            action.run();
            return null;
        }).when(transactionUtils).afterCommit(any());

        service.refundTrip(1, 60);

        assertEquals(0, userProfile.getBalance().compareTo(BigDecimal.ONE));
        assertEquals(BigDecimal.ONE, trip.getTotalPrice());
        assertEquals(60, trip.getDurationSeconds());
        verify(emailService, only()).asyncSideSendMail(
                eq("test@example.com"),
                anyString(),
                anyString()
        );
    }
}
