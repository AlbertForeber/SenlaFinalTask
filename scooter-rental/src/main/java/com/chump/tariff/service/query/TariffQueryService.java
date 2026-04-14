package com.chump.tariff.service.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.tariff.dao.TariffDao;
import com.chump.tariff.dto.response.TariffResponse;
import com.chump.tariff.mapper.TariffMapper;
import com.chump.tariff.model.Tariff;
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
    public List<TariffResponse> getAllTariffs() {
        return tariffMapper.toResponseList(tariffDao.findAll());
    }

    @Transactional(readOnly = true)
    public TariffResponse getTariffById(int tariffId) {
        Tariff result = tariffDao.findById(tariffId).orElseThrow(
                () -> new NoSuchEntityException("No tariff found with id: " + tariffId)
        );

        return tariffMapper.toResponse(result);
    }
}
