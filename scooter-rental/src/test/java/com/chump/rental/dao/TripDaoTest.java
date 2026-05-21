package com.chump.rental.dao;

import com.chump.common.dao.AbstractDaoTest;
import com.chump.common.utils.GeoConverter;
import com.chump.rental.dto.entry.WaypointEntry;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.Trip;
import com.chump.rental.model.status.TripStatus;
import com.chump.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Trip DAO testing")
@ContextConfiguration(classes = {
        TripDao.class,
        TripPointDao.class,
        GeoConverter.class,
        TripDaoTest.Config.class
})
@TestPropertySource(properties =
        "hibernate.jdbc.batch-size=50"
)
public class TripDaoTest extends AbstractDaoTest {

    @Autowired
    private TripDao tripDao;

    @Autowired
    private TripPointDao tripPointDao;

    @Autowired
    private GeoConverter geoConverter;

    @Configuration
    public static class Config {

        @Bean
        public GeometryFactory geometryFactory() {
            return new GeometryFactory(new PrecisionModel(), 4326);
        }
    }

    @Test
    @DisplayName("Save should correctly persist trip and return it's correct id (checked by findById)")
    void saveShouldPersistEntityCorrectly() {
        Instant startedAt = Instant.now();
        Trip trip = buildBaseTrip(TripStatus.ONGOING, startedAt);

        trip = tripDao.save(trip);
        flushAndClear();

        Optional<Trip> savedTrip = tripDao.findById(trip.getId());
        assertTrue(savedTrip.isPresent());
        Trip finalTrip = trip;

        assertAll("Trip must be equal to trip provided to save",
                () -> assertEquals(finalTrip.getScooter().getId(), savedTrip.get().getScooter().getId()),
                () -> assertEquals(finalTrip.getUser().getId(), savedTrip.get().getUser().getId()),
                () -> assertEquals(startedAt, savedTrip.get().getStartedAt())
        );
    }

    @Test
    @DisplayName("Find ongoing by scooter ID should return ongoing trip for certain scooter")
    void findOngoingByScooterIdShouldReturnOngoingTripsForCertainScooter() {
        Instant startedAt = Instant.now();
        Trip ongoingTrip = buildBaseTrip(TripStatus.ONGOING, startedAt);
        tripDao.save(ongoingTrip);
        flushAndClear();

        Optional<Trip> trip = tripDao.findOngoingByScooterId(ongoingTrip.getScooter().getId());
        assertTrue(trip.isPresent());
        assertEquals(ongoingTrip.getUser().getId(), trip.get().getUser().getId(),
                "User ID in found and saved trip should be same");
        assertEquals(TripStatus.ONGOING, trip.get().getStatus(),
                "Found trip's status must be ONGOING");
    }

    @Test
    @DisplayName("Update route and distance should update route data and return it if there're more than two points")
    void updateRouteAndDistanceShouldUpdateAndReturnWhenMoreThanTwoPoints() {
        Trip trip = buildBaseTrip(TripStatus.FINISHED, Instant.now());

        trip = tripDao.save(trip);
        List<WaypointEntry> waypointEntryList = List.of(
                geoConverter.stringToWaypoint(1, "0.0,0.0,0"),
                geoConverter.stringToWaypoint(1, "1.0,0.0,10"),
                geoConverter.stringToWaypoint(1, "1.0,1.0,20")
        );

        tripPointDao.batchSave(trip.getId(), waypointEntryList);
        tripDao.updateRouteAndDistance(trip.getId());
        flushAndClear();

        Optional<Trip> updatedTrip = tripDao.findById(trip.getId());

        assertTrue(updatedTrip.isPresent());
        assertNotNull(updatedTrip.get().getRoute());
        assertNotNull(updatedTrip.get().getDistance());
        assertEquals(221893.8, updatedTrip.get().getDistance(), 1,
                "Distance must be about 221893,8 metres long");
    }

    private Trip buildBaseTrip(TripStatus status, Instant startedAt) {
        return Trip.builder()
                .status(status)
                .scooter(new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        null))
                .user(new User(
                        1,
                        null,
                        null,
                        null
                ))
                .startedAt(startedAt)
                .priceAtStart(BigDecimal.ONE)
                .intervalAtStart(1)
                .discountAtStart(BigDecimal.ZERO)
                .build();
    }
}
