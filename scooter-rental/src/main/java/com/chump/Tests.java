package com.chump;

import com.chump.auth.service.AuthService;
import com.chump.rental.service.RentalService;
import com.chump.rental.service.RentalSpotService;
import com.chump.rental.service.ScooterService;
import com.chump.rental.service.query.RentalSpotQueryService;
import com.chump.rental.service.query.ScooterQueryService;
import com.chump.rental.service.query.TripQueryService;
import com.chump.user.service.UserService;
import com.chump.user.service.query.UserQueryService;
import jakarta.annotation.PostConstruct;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("transactionManager")
public class Tests {

    static private Logger logger = LoggerFactory.getLogger(Tests.class);

    @Autowired
    UserQueryService queryService;

    @Autowired
    UserService service;

    @Autowired
    RentalSpotQueryService rentalSpotQueryService;

    @Autowired
    RentalSpotService rentalSpotService;

    @Autowired
    ScooterQueryService scooterQueryService;

    @Autowired
    ScooterService scooterService;

    @Autowired
    TripQueryService tripQueryService;

    @Autowired
    private RentalService rentalService;
    @Autowired
    private AuthService authService;


    @PostConstruct
    private void test() throws ParseException {
        System.out.println("Tests initialized");
//        logger.info(queryService.getUserProfile(1).toString());
//
//        logger.info(service.updateUserBaseInfo(1, UpdateUserBaseInfoCommand.builder()
//                        .fullName("updated_test_name")
//                        .dateOfBirth(LocalDate.parse("2000-01-01"))
//                        .build()
//        ).toString());

//        logger.info(service.updateUserRole(1, 2).toString());
//        logger.info(rentalQueryService.getRentalSpotHierarchy(1).toString());
//        logger.info(rentalQueryService.getRentalSpotInfo(2).toString());
//        logger.info(
//                rentalSpotQueryService.getNearbySpots(new GeoSearchParams(55.792014f, 37.588687f, 500))
//                        .toString());
//        Polygon polygon = (Polygon) new WKTReader().read("POLYGON((37.60 55.75, 37.65 55.75, 37.65 55.80, 37.60 55.80, 37.60 55.75))");
//        logger.info(rentalSpotService.openPoint(
//                RentalSpotCommand.builder()
//                        .name("test_opened_point")
//                        .area(polygon)
//                        .parentId(1)
//                        .isZone(true)
//                        .build()
//        ).toString());
//
//        logger.info(rentalSpotService.updatePointInfo(
//                3,
//                RentalSpotCommand.builder()
//                        .isZone(false)
//                        .build()
//        ).toString());

//        logger.info(scooterQueryService.getAllFreeScooters().toString());
//        logger.info(scooterQueryService.getNearbyScooters(new GeoSearchParams(
//                55.6760357f, 37.7613831f, 1
//        )).toString());

//        Point point = (Point) new WKTReader().read("POINT(37.60 55.75)");
//        logger.info(scooterService.addScooter(ScooterCommand.builder()
//                .modelId(1)
//                .serialNumber("test_serial_second")
//                .battery(100)
//                .location(point)
//                .status(ScooterStatus.OCCUPIED)
//                .build()).toString());
//
//        logger.info(scooterQueryService.getScooterByStatus(ScooterStatus.OCCUPIED).toString());
//        logger.info(tripQueryService.getOngoingTrips(1).toString());
//        logger.info(tripQueryService.getScooterTrips(1).toString());
//        logger.info(tripQueryService.getUserTrips(1).toString());
//        logger.info(tripQueryService.getTripInfo(1).toString());
//        logger.info(tripQueryService.getTripsFromPeriod(
//                Instant.parse("2025-12-31T10:15:30.00Z"),
//                Instant.parse("2026-01-01T00:00:00.00Z")
//        ).toString());
//
//        logger.info(rentalService.returnScooter(1, 1, false).toString());
//        logger.info(rentalService.rentScooter(1, 1).toString());
//        logger.info(authService.login(LoginCommand.builder().username("test").password("test").build()).toString());
//        logger.info(authService.register(RegisterCommand.builder()
//                        .username("test_register")
//                        .password("test_register")
//                .fullName("test_register")
//                .dateOfBirth(LocalDate.now())
//                .build()).toString());
    }
}