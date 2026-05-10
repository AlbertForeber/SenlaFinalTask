package billing.service;

import com.chump.billing.dao.BillingBatchFailureDao;
import com.chump.billing.dao.BillingBatchFailureItemDao;
import com.chump.billing.dto.response.BillingResponse;
import com.chump.billing.model.BillingBatchFailure;
import com.chump.billing.model.BillingBatchFailureItem;
import com.chump.billing.model.BillingBatchFailureItemId;
import com.chump.billing.service.BillingProcessor;
import com.chump.billing.service.BillingService;
import com.chump.user.dao.UserSubscriptionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Billing service testing")
@ExtendWith(MockitoExtension.class)
public class BillingServiceTest {

    @Mock private UserSubscriptionDao userSubscriptionDao;
    @Mock private BillingProcessor billingProcessor;
    @Mock private BillingBatchFailureDao billingBatchFailureDao;
    @Mock private BillingBatchFailureItemDao billingBatchFailureItemDao;

    @InjectMocks
    private BillingService billingService;

    @BeforeEach
    public void init() {
        ReflectionTestUtils.setField(billingService, "batchSize", 1);
    }

    @Test
    @Tag("unit")
    @DisplayName("Process billing method should return response with correct total success and failed batches")
    public void processBillingShouldReturnCorrectResponse() throws InterruptedException {
        doNothing().doThrow(new RuntimeException(new RuntimeException("test_cause")))
                .when(billingProcessor).processSingleBatch(any());
        when(userSubscriptionDao.batchFindToBillIds(eq(1), anyInt()))
                .thenReturn(Collections.singletonList(1))
                .thenReturn(Collections.singletonList(2))
                .thenReturn(Collections.emptyList());

        BillingResponse response = billingService.processBilling();

        assertAll("Response validation",
                () -> assertEquals(1, response.getTotalSuccess(),
                        "Total successes should equal one"),
                () -> assertEquals(1, response.getTotalFailed(),
                        "Total failed should equal one")
        );
    }

    @Test
    @Tag("unit")
    @DisplayName("Manual billing method should return response with correct total success and failed batches and details")
    public void manualBillingShouldReturnCorrectResponse() throws InterruptedException {
        BillingBatchFailure failure1 = BillingBatchFailure.builder()
                .id(1)
                .errorMessage("test_error")
                .failedAt(null)
                .isResolved(false)
                .build();
        BillingBatchFailure failure2 = BillingBatchFailure.builder()
                .id(1)
                .errorMessage("test_error")
                .failedAt(null)
                .isResolved(false)
                .build();

        doNothing().doThrow(new RuntimeException(new RuntimeException("test_cause")))
                .when(billingProcessor).processSingleBatch(any());
        when(billingBatchFailureItemDao.batchFindNotResolvedWithFailure(anyInt()))
                .thenReturn(Collections.singletonList(new BillingBatchFailureItem(
                        BillingBatchFailureItemId.builder()
                                .failureId(1)
                                .userSubscriptionId(1)
                                .build(),
                        failure1
                )))
                .thenReturn(Collections.singletonList(new BillingBatchFailureItem(
                        BillingBatchFailureItemId.builder()
                                .failureId(1)
                                .userSubscriptionId(1)
                                .build(),
                        failure2
                )))
                .thenReturn(Collections.emptyList());

        BillingResponse response = billingService.manualBilling(true);

        assertAll("Response validation",
                () -> assertEquals(1, response.getTotalSuccess(),
                        "Total successes should equal one"),
                () -> assertEquals(1, response.getTotalFailed(),
                        "Total failed should equal one"),
                () -> assertEquals(1, response.getFailedDetails().size(),
                        "Failed details size should equal one"),
                () -> assertEquals("test_cause", response.getFailedDetails().get(0).getErrorMessage(),
                        "Only detail reason should equal 'test_cause'")
        );

        assertAll("Failures validation",
                () -> assertTrue(failure1.getIsResolved()),
                () -> assertFalse(failure2.getIsResolved())
        );
    }
}
