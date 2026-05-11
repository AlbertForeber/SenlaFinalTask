package com.chump.billing.service;

import com.chump.billing.dao.TariffDao;
import com.chump.billing.dto.command.TariffCommand;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.model.Tariff;
import com.chump.billing.service.TariffService;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tariff service testing")
@ExtendWith(MockitoExtension.class)
public class TariffServiceTest {

    @Mock private TariffDao tariffDao;
    @Mock private TariffMapper tariffMapper;

    @InjectMocks
    private TariffService tariffService;

    @Test
    @Tag("unit")
    @DisplayName("Update tariff method should throw an exception, if tariff ID is unknown")
    public void updateTariffShouldThrowWhenUnknownTariffId() {
        when(tariffDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> tariffService.updateTariff(1, new TariffCommand()));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown tariff ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Update tariff method should throw an exception, if trying to change subscription's interval")
    public void updateTariffShouldThrowWhenIntervalForSubscription() {
        TariffCommand tariffCommand = new TariffCommand();
        tariffCommand.setInterval(1);

        when(tariffDao.findById(anyInt())).thenReturn(Optional.of(
                new Tariff(
                        1,
                        null,
                        null,
                        null
                ))
        );

        assertThrows(UnavaliableActionException.class,
                () -> tariffService.updateTariff(1, tariffCommand));
    }

    @Test
    @Tag("unit")
    @DisplayName("Delete tariff method should throw an exception, if tariff ID is unknown")
    public void deleteTariffShouldThrowWhenUnknownTariffId() {
        when(tariffDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> tariffService.deleteTariff(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown tariff ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Delete tariff method should throw an exception, if trying to delete subscription")
    public void deleteTariffShouldThrowWhenSubscription() {
        when(tariffDao.findById(anyInt())).thenReturn(Optional.of(
                new Tariff(
                        1,
                        null,
                        null,
                        null
                ))
        );

        assertThrows(UnavaliableActionException.class,
                () -> tariffService.deleteTariff(1));
    }
}
