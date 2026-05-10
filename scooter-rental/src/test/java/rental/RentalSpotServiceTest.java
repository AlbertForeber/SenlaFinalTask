package rental;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.rental.dao.RentalSpotDao;
import com.chump.rental.dto.command.RentalSpotCommand;
import com.chump.rental.mapper.RentalSpotMapper;
import com.chump.rental.model.RentalSpot;
import com.chump.rental.service.RentalSpotService;
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

@DisplayName("Rental spot service testing")
@ExtendWith(MockitoExtension.class)
public class RentalSpotServiceTest {

    @Mock private RentalSpotMapper rentalSpotMapper;
    @Mock private RentalSpotDao rentalSpotDao;

    @InjectMocks
    private RentalSpotService service;

    @Test
    @Tag("unit")
    @DisplayName("Open point method should throw exception, if parent ID is unknown")
    public void openPointShouldThrowWhenUnknownParentId() {
        when(rentalSpotDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.openPoint(RentalSpotCommand.builder().parentId(1).build()));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown parent ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Update point method should throw exception, if parent ID is unknown")
    public void updatePointShouldThrowWhenUnknownParentId() {
        when(rentalSpotDao.findById(1)).thenReturn(Optional.of(
                new RentalSpot(
                        1,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        ));
        when(rentalSpotDao.findById(2)).thenReturn(Optional.empty());


        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.updatePointInfo(1, RentalSpotCommand.builder().parentId(2).build()));
        assertTrue(exception.getMessage().contains("2"),
                "Exception message should contain unknown parent ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Update point method should throw exception, if rental spot ID is unknown")
    public void updatePointShouldThrowWhenUnknownSpotId() {
        when(rentalSpotDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.updatePointInfo(1, RentalSpotCommand.builder().parentId(2).build()));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown spot ID");
    }
}