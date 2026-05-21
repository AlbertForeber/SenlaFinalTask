package com.chump.rental.dao;


import com.chump.common.dao.AbstractDaoTest;
import com.chump.common.utils.GeoConverter;
import com.chump.rental.model.RentalSpot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testing rental spot dao test")
@ContextConfiguration(classes = {
        RentalSpotDao.class,
        RentalSpotDaoTest.Config.class,
        GeoConverter.class
})
public class RentalSpotDaoTest extends AbstractDaoTest {

    @Autowired
    private RentalSpotDao rentalSpotDao;

    @Autowired
    private GeometryFactory geometryFactory;

    @Configuration
    public static class Config {

        @Bean
        public GeometryFactory geometryFactory() {
            return new GeometryFactory(new PrecisionModel(), 4326);
        }
    }

    @Test
    @DisplayName("Is in parking rental spot should true if given point is in any rental spot parking")
    public void isInParkingRentalSpotShouldReturnTrueIfInParkingSpot() {
        saveTestSpot();
        Point testLocation = geometryFactory.createPoint(new Coordinate(0.999, 0.999));
        assertTrue(rentalSpotDao.isInParkingRentalSpot(testLocation));
    }

    @Test
    @DisplayName("Is in parking rental spot should false if given point is not in any rental spot parking")
    public void isInParkingRentalSpotShouldReturnFalseIfNotInParkingSpot() {
        saveTestSpot();
        Point testLocation = geometryFactory.createPoint(new Coordinate(1, 1));
        assertFalse(rentalSpotDao.isInParkingRentalSpot(testLocation));
    }

    private void saveTestSpot() {
        RentalSpot testSpot = new RentalSpot(
                null,
                null,
                Collections.emptyList(),
                "test_parking",
                geometryFactory.createPolygon(new Coordinate[]{
                        new Coordinate(0, 0),
                        new Coordinate(0, 1),
                        new Coordinate(1, 1),
                        new Coordinate(1, 0),
                        new Coordinate(0, 0)
                }),
                true
        );

        rentalSpotDao.save(testSpot);
        flushAndClear();
    }
}
