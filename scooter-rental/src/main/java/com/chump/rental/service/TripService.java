package com.chump.rental.service;

import com.chump.common.exception.NoRequiredEntityException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.common.utils.TransactionUtils;
import com.chump.common.utils.TripPriceCalculator;
import com.chump.notification.service.EmailService;
import com.chump.rental.dao.TripDao;
import com.chump.rental.dto.response.TripRefundResponse;
import com.chump.rental.model.Trip;
import com.chump.rental.model.status.TripStatus;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class TripService {

    private final TripDao tripDao;
    private final UserProfileDao userProfileDao;
    private final EmailService emailService;

    public TripService(TripDao tripDao, UserProfileDao userProfileDao, EmailService emailService) {
        this.tripDao = tripDao;
        this.userProfileDao = userProfileDao;
        this.emailService = emailService;
    }

    // refundForLastSeconds - позволяет вернуть деньги только за последние n секунд поездки (расчет учитывает тариф)
    @Transactional
    public TripRefundResponse refundTrip(int tripId, int refundForLastSeconds) {
        Trip trip = tripDao.findById(tripId).orElseThrow(
                () -> new NoSuchEntityException("No trip found with id: " + tripId)
        );

        if (trip.getStatus() == TripStatus.ONGOING) {
            throw new UnavaliableActionException("Forbidden to refund ongoing trip with id: " + tripId);
        }

        // Не используем equalTo, чтобы игнорировать разницу в scale
        if (trip.getTotalPrice().compareTo(BigDecimal.ZERO) == 0) {
            throw new UnavaliableActionException("Trip with id " + tripId + " is already fully refunded");
        }

        UserProfile userProfile = userProfileDao.findById(trip.getUser().getId()).orElseThrow(
                () -> new NoRequiredEntityException("No user profile found for trip with id: " + tripId)
        );

        TransactionUtils.afterCommit(() -> {
            emailService.asyncSideSendMail(userProfile.getEmail(), "Refund", "One of your trips have been refunded");
        });

        if (refundForLastSeconds == 0 || refundForLastSeconds >= trip.getDurationSeconds()) {
            userProfile.setBalance(userProfile.getBalance().add(trip.getTotalPrice()));
            BigDecimal oldPrice = trip.getTotalPrice();

            trip.setTotalPrice(BigDecimal.ZERO);

            return TripRefundResponse.builder()
                    .userId(userProfile.getId())
                    .newBalance(userProfile.getBalance())
                    .refunded(oldPrice)
                    .build();
        }

        trip.setDurationSeconds(trip.getDurationSeconds() - refundForLastSeconds);
        BigDecimal newPrice = TripPriceCalculator.calculatePrice(trip);
        BigDecimal oldPrice = trip.getTotalPrice();

        userProfile.setBalance(
                userProfile.getBalance()
                        .add(trip.getTotalPrice())
                        .subtract(newPrice)
        );

        trip.setTotalPrice(newPrice);

        return TripRefundResponse.builder()
                .userId(userProfile.getId())
                .newBalance(userProfile.getBalance())
                .refunded(oldPrice.subtract(newPrice))
                .build();
    }
}
