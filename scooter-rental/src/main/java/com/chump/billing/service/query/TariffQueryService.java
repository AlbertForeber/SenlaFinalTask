package com.chump.billing.service.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.billing.dao.TariffDao;
import com.chump.billing.dto.response.TariffConciseResponse;
import com.chump.billing.dto.response.TariffDetailedResponse;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.model.Tariff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TariffQueryService {

    private final TariffDao tariffDao;
    private final TariffMapper tariffMapper;

    public TariffQueryService(TariffDao tariffDao, TariffMapper tariffMapper) {
        this.tariffDao = tariffDao;
        this.tariffMapper = tariffMapper;
    }

    @Transactional(readOnly = true)
    public List<TariffConciseResponse> getAllTariffs() {
        return tariffMapper.toConciseResponseList(tariffDao.findAll());
    }

    @Transactional(readOnly = true)
    public TariffDetailedResponse getTariffById(int tariffId) {
        Tariff result = tariffDao.findById(tariffId).orElseThrow(
                () -> new NoSuchEntityException("No tariff found with id: " + tariffId)
        );

        return tariffMapper.toDetailedResponse(result);
    }
}
