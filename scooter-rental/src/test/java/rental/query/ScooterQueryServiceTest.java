package rental.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.rental.dao.ScooterModelDao;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.mapper.ScooterModelMapper;
import com.chump.rental.repo.ScooterRepository;
import com.chump.rental.service.query.ScooterQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@DisplayName("Scooter query service testing")
@ExtendWith(MockitoExtension.class)
public class ScooterQueryServiceTest {

    @Mock private ScooterRepository scooterRepository;
    @Mock private ScooterModelDao scooterModelDao;
    @Mock private ScooterMapper scooterMapper;
    @Mock private ScooterModelMapper modelMapper;

    @InjectMocks
    private ScooterQueryService service;

    @Test
    @Tag("unit")
    @DisplayName("Get scooter info method should throw an exception, if scooter ID is unknown")
    public void getInfoShouldThrowWhenUnknownScooterId() {
        when(scooterRepository.findByIdWithActualInfoAndModel(anyInt())).thenReturn(Optional.empty());
        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.getScooterInfo(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown scooter ID");
    }
}