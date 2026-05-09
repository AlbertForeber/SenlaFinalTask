package billing.service.query;

import com.chump.billing.dao.TariffDao;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.service.query.SubscriptionQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SubscriptionQueryServiceTest {

    @Mock private TariffDao tariffDao;
    @Mock private TariffMapper tariffMapper;

    @InjectMocks
    private SubscriptionQueryService service;

    @Test
    @Tag("unit")
    @DisplayName("Get subscription method should return subscription")
    public void getSubscriptionShouldReturnSubscription() {

    }
}
