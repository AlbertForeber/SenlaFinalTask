package com.chump.rental.service;

import com.chump.common.exception.InaccessibleActionException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.rental.dao.RentalSpotDao;
import com.chump.rental.dao.ScooterPendingRedisDao;
import com.chump.rental.dao.TripDao;
import com.chump.rental.dao.TripPointDao;
import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.dto.response.TripDetailedResponse;
import com.chump.rental.kafka.ScooterProducer;
import com.chump.rental.mapper.TripMapper;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.Trip;
import com.chump.rental.model.TripPoint;
import com.chump.rental.model.status.ScooterStatus;
import com.chump.rental.model.status.TripStatus;
import com.chump.rental.repo.ScooterRepository;
import com.chump.billing.dao.TariffDao;
import com.chump.billing.model.Tariff;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.dao.UserSubscriptionDao;
import com.chump.user.model.UserProfile;
import com.chump.user.model.UserSubscription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

// TODO Связь с Kafka
// TODO + должен отправлять сообщение самокату, что он разблокирован/заблокирован
// TODO + самокат должен начать отправлять данные местоположения в соответствующий топик
// TODO        откуда, система должна их считывать и записывать в Redis
// TODO + если пользователь захочет посмотреть поездку до ее завершения - данные подтягиваются из Redis
// TODO + после завершения поездки данные отправляются в БД (упростить линию)
@Service
@RequiredArgsConstructor
public class RentalService {

    private final ScooterRepository scooterRepository;
    private final TripDao tripDao;
    private final TripMapper tripMapper;
    private final UserProfileDao userProfileDao;
    private final UserSubscriptionDao userSubscriptionDao;
    private final TariffDao tariffDao;
    private final TripPointDao tripPointDao;
    private final RentalSpotDao rentalSpotDao;
    private final ScooterProducer scooterProducer;
    private final ScooterPendingRedisDao scooterPendingRedisDao;

    @Value("${rental.minimal-balance}")
    private Integer minToStart;

    @AllArgsConstructor
    @Getter
    private static class UserContext {
        private UserProfile userProfile;
        private Tariff tariff;
    }

    @Transactional
    public TripConciseResponse rentScooter(int scooterId, int userId) {
        Scooter scooter = findAndValidateScooter(scooterId);
        UserContext userContext = collectUserContext(userId);
        UserProfile userProfile = userContext.getUserProfile();
        Tariff tariff = userContext.getTariff();

        if (userProfile.getBalance().longValue() < minToStart && tariff.getBillingIntervalMinutes() != null) {
            throw new UnavaliableActionException("Not enough money to rent a scooter");
        }

        // TODO  Сообщение самокату в Kafka разблокируйся + начни отправлять данные в waypoint-topic
        // TODO Создание флага ожидания в REDIS
        scooter.setStatus(ScooterStatus.ACTIVATING);
        scooterPendingRedisDao.setPending(scooterId);
        scooterProducer.sendUnlock(scooterId);

        Trip createdTrip = tripDao.save(Trip.builder()
                .status(TripStatus.ONGOING)
                .scooter(scooter)
                .user(userProfile.getUser())
                .startedAt(Instant.now())
                .priceAtStart(tariff.getBasePrice())
                .intervalAtStart(tariff.getBillingIntervalMinutes())
                .discountAtStart(userProfile.getDiscount())
                .build());


        return tripMapper.toConciseResponse(createdTrip);
    }

    @Transactional
    public TripConciseResponse pauseScooter(int scooterId, int userId) {
        Trip ongoingTrip = findAndValidateTrip(scooterId, userId);
        ongoingTrip.setStatus(TripStatus.PAUSED);

        // TODO Сообщение самокату в Kafka заблокируйся + приостановка отправки в waypoint
        // TODO Создание флага ожидания в REDIS
        scooterProducer.sendLock(scooterId);
        return tripMapper.toConciseResponse(ongoingTrip);
    }

    @Transactional
    public TripConciseResponse resumeScooter(int scooterId, int userId) {
        Trip ongoingTrip = findAndValidateTrip(scooterId, userId);
        ongoingTrip.setStatus(TripStatus.ONGOING);
        scooterPendingRedisDao.setPending(scooterId);
        // TODO Сообщение самокату в Kafka разблокируйся + начни отправлять данные в waypoint-topic
        // TODO Создание флага ожидания в REDIS

        scooterProducer.sendUnlock(scooterId);
        return tripMapper.toConciseResponse(ongoingTrip);
    }

    @Transactional
    public TripDetailedResponse returnScooter(int scooterId, int userId, boolean isForce) {
        Trip ongoingTrip = findAndValidateTrip(scooterId, userId);
        finishTrip(ongoingTrip, scooterId, userId, isForce);

        List<TripPoint> waypoints = tripPointDao.findByTripId(ongoingTrip.getId());
        ongoingTrip.setDistance(calculateDistance(waypoints));

        return tripMapper.toDetailedResponse(ongoingTrip, waypoints);
    }

    private Scooter findAndValidateScooter(int scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        if (scooter.getStatus() != ScooterStatus.FREE) {
            throw new InaccessibleActionException("Forbidden to rent occupied scooter");
        }

        return scooter;
    }

    private Trip findAndValidateTrip(int scooterId, int userId) {
        Trip trip = tripDao.findOngoingByScooterId(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No trip found for scooter with id: " + scooterId)
        );

        if (!trip.getUser().getId().equals(userId)) {
            throw new InaccessibleActionException("Forbidden to manage someone else's scooter");
        }

        return trip;
    }

    private void finishTrip(Trip trip, int scooterId, int userId, boolean isForce) {
        Scooter scooter = scooterRepository.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        // Проверка зоны парковки
        if (!rentalSpotDao.isInParkingRentalSpot(scooter.getLocation()) && !isForce) {
            throw new UnavaliableActionException("Scooter should be in rental zone");
        }

        UserProfile userProfile = userProfileDao.findById(userId).orElseThrow(
                () -> new NoSuchEntityException("No user profile found with id:" + userId)
        );

        Duration duration = Duration.between(trip.getStartedAt(), Instant.now());
        trip.setStatus(TripStatus.FINISHED);

        scooter.setStatus(isForce ? ScooterStatus.BLOCKING : ScooterStatus.STOPPING);

        // TODO таймер Redis
        scooterPendingRedisDao.setPending(scooterId);
        scooterProducer.sendLock(scooterId);

        trip.setDurationSeconds((int) duration.toSeconds());
        trip.setTotalPrice(calculatePrice(trip));

        userProfile.setBalance(userProfile.getBalance().subtract(trip.getTotalPrice()));
    }

    private float calculateDistance(List<TripPoint> waypoints) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        LineString line = geometryFactory.createLineString(
                waypoints.stream()
                        .map(p -> new Coordinate(p.getLocation().getX(), p.getLocation().getY()))
                        .toArray(Coordinate[]::new)
        );

        // TODO временная логика, получаем результат в градусах
        // TODO умножение на примерное значение, чтобы не писать лишний запрос в БД
        // TODO далее будет замененно на Redis
        // TODO + в БД отправляется упрощенный маршрут
        return (float) line.getLength() * 111000;
    }

    private BigDecimal calculatePrice(Trip trip) {
        BigDecimal price = trip.getPriceAtStart();
        Integer interval = trip.getIntervalAtStart();
        BigDecimal discount = trip.getDiscountAtStart();

        BigDecimal fullPrice = null;

        if (interval != null) {
            fullPrice = price
                    .multiply(
                            BigDecimal.valueOf(trip.getDurationSeconds())
                                    .divide(BigDecimal.valueOf(60L * interval), 2, RoundingMode.UP));
            fullPrice = fullPrice.subtract(fullPrice.multiply(discount));
        }
        return fullPrice;
    }

    private UserContext collectUserContext(int userId) {
        UserProfile userProfile = userProfileDao.findById(userId).orElseThrow(
                () -> new NoSuchEntityException("No user profile found for trip with id: " + userId
                        + ". Contact support service.")
        );

        UserSubscription userSubscription = userSubscriptionDao.findByIdWithTariff(userId).orElse(null);

        Tariff tariff = userSubscription != null ? userSubscription.getTariff() :
                tariffDao
                        .getDefaultTariff()
                        .orElseThrow(
                                () -> new NoSuchEntityException("No default tariff found. Contact support service.")
                        );

        return new UserContext(userProfile, tariff);
    }
}
