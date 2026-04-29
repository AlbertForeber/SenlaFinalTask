package com.chump.rental.service;

import com.chump.rental.dao.ScooterPostgresDao;
import com.chump.rental.dto.entry.TelemetryEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelemetryProcessor {

    private final ScooterPostgresDao scooterPostgresDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleBatch(List<TelemetryEntry> entries) {
        scooterPostgresDao.batchUpdateTelemetry(entries);
    }
}
