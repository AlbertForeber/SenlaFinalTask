package com.chump.billing.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.billing.dao.TariffDao;
import com.chump.billing.dto.command.TariffCommand;
import com.chump.billing.dto.response.TariffDetailedResponse;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.model.Tariff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TariffService {

    private final TariffDao tariffDao;
    private final TariffMapper tariffMapper;

    public TariffService(TariffDao tariffDao,
                         TariffMapper tariffMapper) {
        this.tariffDao = tariffDao;
        this.tariffMapper = tariffMapper;
    }

    @Transactional
    public TariffDetailedResponse updateTariff(int tariffId, TariffCommand command) {
        Tariff tariff = tariffDao.findById(tariffId).orElseThrow(
                () -> new NoSuchEntityException("No tariff found with id: " + tariffId)
        );

        if (command.getInterval() != null && tariff.getBillingIntervalMinutes() == null) {
            throw new UnavaliableActionException("Cannot change interval of subscription");
        }

        tariffMapper.updateTariffFromCommand(command, tariff);
        return tariffMapper.toDetailedResponse(tariff);
    }

    @Transactional
    public TariffDetailedResponse addTariff(TariffCommand command) {
        Tariff tariff = tariffMapper.toEntity(command);
        return tariffMapper.toDetailedResponse(tariffDao.save(tariff));
    }

    @Transactional
    public void deleteTariff(int tariffId) {
        Tariff tariff = tariffDao.findById(tariffId).orElseThrow(
                () -> new NoSuchEntityException("No tariff found with id: " + tariffId)
        );

        if (tariff.getBillingIntervalMinutes() == null) {
            throw new UnavaliableActionException("Cannot delete subscription. Use /api/tariff/subscription instead");
        }

        tariffDao.delete(tariffId);
    }
}
