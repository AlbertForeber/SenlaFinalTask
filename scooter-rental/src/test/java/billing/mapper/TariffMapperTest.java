package billing.mapper;

import com.chump.billing.dto.response.TariffDetailedResponse;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.model.Tariff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tariff mapper testing")
public class TariffMapperTest {

    private TariffMapper tariffMapper;

    @BeforeEach
    public void init() {
        tariffMapper = Mappers.getMapper(TariffMapper.class);
    }

    @Test
    @Tag("unit")
    @DisplayName("To detailed response method should replace billing interval to subscription, if interval is null")
    public void toDetailedResponseShouldReturnSubscriptionWhenNullInterval() {
        Tariff tariff = new Tariff(
                1,
                "test_name",
                BigDecimal.ZERO,
                null
        );
        TariffDetailedResponse response = tariffMapper.toDetailedResponse(tariff);
        assertEquals("SUBSCRIPTION", response.getBillingIntervalMinutes(),
                "Response should have SUBSCRIPTION in interval field");
    }

    @Test
    @Tag("unit")
    @DisplayName("To detailed response method should return billing interval, if interval is not null")
    public void toDetailedResponseShouldReturnIntervalWhenNotNullInterval() {
        Tariff tariff = new Tariff(
                1,
                "test_name",
                BigDecimal.ZERO,
                1
        );
        TariffDetailedResponse response = tariffMapper.toDetailedResponse(tariff);
        assertEquals("1", response.getBillingIntervalMinutes(),
                "Response should have interval in interval field");
    }
}
