package rental.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.rental.dao.RentalSpotDao;
import com.chump.rental.mapper.RentalSpotMapper;
import com.chump.rental.repo.ScooterRepository;
import com.chump.rental.service.query.RentalSpotQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Rental spot query service testing")
@ExtendWith(MockitoExtension.class)
public class RentalSpotQueryServiceTest {

    @Mock private RentalSpotDao rentalSpotDao;
    @Mock private ScooterRepository scooterRepository;
    @Mock private RentalSpotMapper rentalSpotMapper;

    @InjectMocks
    private RentalSpotQueryService service;

    @Test
    @Tag("unit")
    @DisplayName("Get rental spot hierarchy up method should throw an exception, if rental spot ID is unknown")
    public void getHierarchyShouldThrowWhenUnknownRentalSpotId() {
        when(rentalSpotDao.findByIdWithParents(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.getRentalSpotHierarchyUp(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown rental spot ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Get rental spot scooters method should throw an exception, if rental spot ID is unknown")
    public void getScootersShouldThrowWhenUnknownRentalSpotId() {
        when(rentalSpotDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.getRentalSpotScooters(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown rental spot ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Get rental spot detailed info method should throw an exception, if rental spot ID is unknown")
    public void getDetailedInfoShouldThrowWhenUnknownRentalSpotId() {
        when(rentalSpotDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.getRentalSpotsDetailedInfo(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown rental spot ID");
    }
}
