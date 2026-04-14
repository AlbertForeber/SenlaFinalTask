package com.chump.tariff.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.tariff.dao.TariffDao;
import com.chump.tariff.dto.response.TariffResponse;
import com.chump.tariff.mapper.TariffMapper;
import com.chump.tariff.model.Tariff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TariffService {

    private final TariffDao tariffDao;
    private final TariffMapper tariffMapper;

    public TariffService(TariffDao tariffDao, TariffMapper tariffMapper) {
        this.tariffDao = tariffDao;
        this.tariffMapper = tariffMapper;
    }

    @Transactional
    public TariffResponse updateTariffBasePrice(int tariffId, BigDecimal newPrice) {
        Tariff tariff = tariffDao.findById(tariffId).orElseThrow(
                () -> new NoSuchEntityException("No tariff found with id: " + tariffId)
        );

        tariff.setBasePrice(newPrice);
        return tariffMapper.toResponse(tariff);
    }
}
