package com.chump.billing.service.query;

import com.chump.billing.dao.BillingBatchFailureDao;
import com.chump.billing.dto.response.BillingBatchFailureResponse;
import com.chump.billing.mapper.BillingMapper;
import com.chump.common.utils.TransactionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingFailureQueryService {

    private final BillingBatchFailureDao billingBatchFailureDao;
    private final BillingMapper billingMapper;
    private final TransactionUtils transactionUtils;

    @Transactional(readOnly = true)
    public List<BillingBatchFailureResponse> getAllBillingFailures(int pageSize, int page) {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got all billing failures")
        );

        return billingMapper.toResponseList(
                billingBatchFailureDao.batchFindAllWithItemsSortedByFailedAt(pageSize, page - 1)
        );
    }
}
