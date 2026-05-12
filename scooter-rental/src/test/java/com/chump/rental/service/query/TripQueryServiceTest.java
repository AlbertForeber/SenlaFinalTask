package com.chump.rental.service.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.rental.dao.ScooterModelDao;
import com.chump.rental.dao.TripDao;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.mapper.ScooterModelMapper;
import com.chump.rental.mapper.TripMapper;
import com.chump.rental.model.Trip;
import com.chump.rental.model.status.TripStatus;
import com.chump.rental.repo.ScooterRepository;
import com.chump.rental.service.query.ScooterQueryService;
import com.chump.rental.service.query.TripQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@DisplayName("Trip query service testing")
@ExtendWith(MockitoExtension.class)
public class TripQueryServiceTest {

    @Mock private TripDao tripDao;
    @Mock private TripMapper tripMapper;

    @InjectMocks
    private TripQueryService service;

    @Test
    @Tag("unit")
    @DisplayName("Get trip info method should throw an exception, if trip ID is unknown")
    public void getInfoShouldThrowWhenUnknownTripId() {
        when(tripDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.getTripInfo(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown trip ID");
    }
}