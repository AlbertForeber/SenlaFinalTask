package com.chump.billing.service.query;

import com.chump.billing.dao.BillingBatchFailureDao;
import com.chump.billing.dto.response.BillingBatchFailureResponse;
import com.chump.billing.mapper.BillingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingFailureQueryService {

    private final BillingBatchFailureDao billingBatchFailureDao;
    private final BillingMapper billingMapper;

    public List<BillingBatchFailureResponse> getAllBillingFailures(int pageSize, int page) {
        return billingMapper.toResponseList(
                billingBatchFailureDao.batchFindAllWithItemsSortedByFailedAt(pageSize, page + 1)
        );
    }
}
