package com.chump.rental.mapper;

import com.chump.rental.dto.response.RentalSpotWithScootersResponse;
import com.chump.rental.mapper.RentalSpotMapper;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.mapper.ScooterModelMapper;
import com.chump.rental.model.RentalSpot;
import com.chump.rental.model.Scooter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Rental spot mapper testing")
public class RentalSpotMapperTest {

    private RentalSpotMapper rentalSpotMapper;

    @BeforeEach
    public void init() {
        ScooterMapper scooterMapper = Mappers.getMapper(ScooterMapper.class);
        ScooterModelMapper scooterModelMapper = Mappers.getMapper(ScooterModelMapper.class);

        rentalSpotMapper = Mappers.getMapper(RentalSpotMapper.class);
        ReflectionTestUtils.setField(scooterMapper, "scooterModelMapper", scooterModelMapper);
        ReflectionTestUtils.setField(rentalSpotMapper, "scooterMapper", scooterMapper);
    }

    @Test
    @Tag("unit")
    @DisplayName("To with scooters response method should count scooters, if there're scooters")
    public void toWithScootersResponseShouldCountScootersWhenScooters() {
        RentalSpot spot = new RentalSpot(
                1,
                null,
                Collections.emptyList(),
                null,
                null,
                null
        );

        List<Scooter> scooters = Collections.singletonList(new Scooter(
                1,
                null,
                null,
                null,
                null,
                null
        ));

        RentalSpotWithScootersResponse response = rentalSpotMapper.toWithScootersResponse(spot, scooters);
        assertEquals(1, response.getTotalScooterAmount());
    }

    @Test
    @Tag("unit")
    @DisplayName("To with scooters response method should set 0, if there aren't scooters")
    public void toWithScootersResponseShouldSetZeroWhenNoScooters() {
        RentalSpot spot = new RentalSpot(
                1,
                null,
                Collections.emptyList(),
                null,
                null,
                null
        );

        List<Scooter> scooters = Collections.emptyList();

        RentalSpotWithScootersResponse response = rentalSpotMapper.toWithScootersResponse(spot, scooters);
        assertEquals(0, response.getTotalScooterAmount());
    }
}
