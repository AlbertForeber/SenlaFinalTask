package com.chump.rental.service;

import com.chump.rental.dao.ScooterTelemetryRedisDao;
import com.chump.rental.dto.entry.TelemetryEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {

    private final TelemetryProcessor telemetryProcessor;
    private final ScooterTelemetryRedisDao scooterTelemetryRedisDao;

    @Value("${telemetry.batch.size}")
    private int batchSize;

    @Transactional(readOnly = true)
    public void processTelemetryUpdate() {
        int successful = 0;
        int failed = 0;
        Instant start = Instant.now();
        List<TelemetryEntry> batch = scooterTelemetryRedisDao.batchFind(batchSize);

        while (!batch.isEmpty()) {
            try {
                telemetryProcessor.processSingleBatch(batch);
                successful += batch.size();
            } catch (Exception e) {
                log.error("Telemetry update failed", e);
                failed += batch.size();
            } finally {
                clearBatch(batch);
                batch = scooterTelemetryRedisDao.batchFind(batchSize);
            }
        }

        log.info("Telemetry flush completed in {} ms. Successful: {}, failed: {}.",
                Duration.between(start, Instant.now()).toMillis(),
                successful,
                failed);
    }

    private void clearBatch(List<TelemetryEntry> batch) {
        try {
            scooterTelemetryRedisDao.deleteByScooterIds(batch.stream().map(TelemetryEntry::getScooterId).toList());
        } catch (Exception e) {
            log.error("Failed to clear telemetry batch", e);
        }
    }
}
