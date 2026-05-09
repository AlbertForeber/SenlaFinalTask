package com.chump.billing.service;

import com.chump.common.utils.TransactionUtils;
import com.chump.notification.service.EmailService;
import com.chump.user.dao.UserSubscriptionDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.lang.Thread.sleep;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingProcessor {

    private final UserSubscriptionDao userSubscriptionDao;
    private final EmailService emailService;

    @SuppressWarnings("BusyWait")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleBatch(List<Integer> batchIds) throws InterruptedException {
        int attempts = 0;
        int delay = 500;

        while (true) {
            try {
                List<String> emailsToNotify = userSubscriptionDao.batchDeleteUnableToPayReturnMails(batchIds);
                userSubscriptionDao.batchProcessBilling(batchIds);
                userSubscriptionDao.batchUpdateLastBillingDate(batchIds);
                TransactionUtils.afterCommit(() -> emailsToNotify.forEach(
                        o -> emailService.asyncSideSendMail(
                                o,
                                "Billing",
                                "Your subscription has been canceled due to insufficient funds in your balance"
                        )
                ));

                return;
            } catch (Exception e) {
                if (attempts == 3 || !isRetryable(e)) throw e;
                log.warn("Billing batch processing failed. Retry {}/3", attempts);
                sleep(delay);

                attempts++;
                delay *= 2;
            }
        }
    }

    private boolean isRetryable(Exception e) {
        // Пробуем еще раз, если deadlock / Ошибка связи к БД
        return e instanceof LockAcquisitionException || e instanceof JDBCConnectionException;
    }
}
