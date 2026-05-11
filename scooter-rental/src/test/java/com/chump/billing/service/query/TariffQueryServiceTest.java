package com.chump.billing.service.query;

import com.chump.billing.dao.TariffDao;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.service.query.TariffQueryService;
import com.chump.common.exception.NoSuchEntityException;
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


@ExtendWith(MockitoExtension.class)
@DisplayName("Tariff query service testing")
public class TariffQueryServiceTest {

    @Mock private TariffDao tariffDao;
    @Mock private TariffMapper tariffMapper;

    @InjectMocks
    private TariffQueryService tariffQueryService;

    @Test
    @Tag("unit")
    @DisplayName("Get tariff method should throw, if tariff ID is unknown")
    public void getTariffShouldThrowWhenUnknown() {
        when(tariffDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> tariffQueryService.getTariffById(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown tariff ID");
    }
}
