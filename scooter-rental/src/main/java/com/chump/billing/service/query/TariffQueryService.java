package com.chump.billing.service.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.billing.dao.TariffDao;
import com.chump.billing.dto.response.TariffConciseResponse;
import com.chump.billing.dto.response.TariffDetailedResponse;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.model.Tariff;
import com.chump.common.utils.TransactionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TariffQueryService {

    private final TariffDao tariffDao;
    private final TariffMapper tariffMapper;
    private final TransactionUtils transactionUtils;

    @Transactional(readOnly = true)
    public List<TariffConciseResponse> getAllTariffs(int pageSize, int page) {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got all tariffs")
        );

        return tariffMapper.toConciseResponseList(tariffDao.batchFindAll(pageSize, page - 1));
    }

    @Transactional(readOnly = true)
    public TariffDetailedResponse getTariffById(int tariffId) {
        Tariff result = tariffDao.findById(tariffId).orElseThrow(
                () -> new NoSuchEntityException("No tariff found with id: " + tariffId)
        );

        transactionUtils.afterCommit(() ->
                log.info("Successfully got tariff with id: {}", tariffId)
        );

        return tariffMapper.toDetailedResponse(result);
    }
}
