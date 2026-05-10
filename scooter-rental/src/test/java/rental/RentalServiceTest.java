package rental;

import com.chump.billing.dao.TariffDao;
import com.chump.common.utils.TransactionUtils;
import com.chump.rental.dao.*;
import com.chump.rental.kafka.ScooterProducer;
import com.chump.rental.mapper.TripMapper;
import com.chump.rental.repo.ScooterRepository;
import com.chump.rental.service.RentalService;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.dao.UserSubscriptionDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Rental service testing")
@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {

    @Mock private ScooterRepository scooterRepository;
    @Mock private TripDao tripDao;
    @Mock private TripMapper tripMapper;
    @Mock private UserProfileDao userProfileDao;
    @Mock private UserSubscriptionDao userSubscriptionDao;
    @Mock private TariffDao tariffDao;
    @Mock private TripPointDao tripPointDao;
    @Mock private RentalSpotDao rentalSpotDao;
    @Mock private ScooterProducer scooterProducer;
    @Mock private ScooterPendingRedisDao scooterPendingRedisDao;
    @Mock private ScooterWaypointRedisDao scooterWaypointRedisDao;
    @Mock private TripTimeLimitRedisDao tripTimeLimitRedisDao;
    @Mock private TransactionUtils transactionUtils;

    @InjectMocks
    private RentalService service;
}
