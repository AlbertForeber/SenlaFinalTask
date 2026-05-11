package com.chump.billing.service;

import com.chump.billing.service.BillingProcessor;
import com.chump.common.utils.TransactionUtils;
import com.chump.notification.service.EmailService;
import com.chump.user.dao.UserSubscriptionDao;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Billing processor testing")
@ExtendWith(MockitoExtension.class)
public class BillingProcessorTest {

    @Mock private UserSubscriptionDao userSubscriptionDao;
    @Mock private EmailService emailService;
    @Mock private TransactionUtils transactionUtils;

    @InjectMocks
    private BillingProcessor billingProcessor;

    @Test
    @Tag("unit")
    @Tag("slow")
    @DisplayName("Billing batch processing should try three times before throwing an exception for deadlock or connection exception")
    public void shouldNotThrowUntilThreeAttempts() {
        when(userSubscriptionDao.batchDeleteUnableToPayReturnMails(any()))
                .thenThrow(new LockAcquisitionException("test_exception", null))
                .thenThrow(new LockAcquisitionException("test_exception", null))
                .thenReturn(Collections.singletonList("test_mail@example.com"));

        doAnswer(invocationOnMock -> {
            Runnable runnable = invocationOnMock.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionUtils).afterCommit(any());

        assertDoesNotThrow(() -> billingProcessor.processSingleBatch(Collections.emptyList()));
        verify(emailService, only()).asyncSideSendMail(eq("test_mail@example.com"), anyString(), anyString());
    }

    @Test
    @Tag("unit")
    @Tag("slow")
    @DisplayName("Billing batch processing should throw an exception after three tries")
    public void shouldThrowAfterThreeAttempts() {
        when(userSubscriptionDao.batchDeleteUnableToPayReturnMails(any()))
                .thenThrow(new LockAcquisitionException("test_exception", null))
                .thenThrow(new LockAcquisitionException("test_exception", null))
                .thenThrow(new LockAcquisitionException("test_exception", null));

        assertThrows(LockAcquisitionException.class, () -> billingProcessor.processSingleBatch(Collections.emptyList()));
    }
}
