package com.chump.rental.repo;

import com.chump.common.dto.param.GeoSearchParams;
import com.chump.rental.dao.ScooterPostgresDao;
import com.chump.rental.dao.ScooterTelemetryRedisDao;
import com.chump.rental.dto.entry.TelemetryEntry;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.status.ScooterStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ScooterRepository {

    private final ScooterPostgresDao postgresDao;
    private final ScooterTelemetryRedisDao redisDao;

    public List<Scooter> findAll() {
        return postgresDao.findAll();
    }

    public Optional<Scooter> findById(int scooterId) {
        try {
            TelemetryEntry telemetryEntry = redisDao.findById(scooterId);
            log.info("Got telemetry {}", telemetryEntry); // TODO убрать
            postgresDao.updateTelemetry(telemetryEntry);
        } catch (Exception e) {
            log.warn("Failed to refresh telemetry from redis. Skipping", e); // TODO Убрать e
        }

        return postgresDao.findById(scooterId);
    }

    public Scooter save(Scooter entity) {
        return postgresDao.save(entity);
    }

    public void update(Scooter entity) {
        postgresDao.update(entity);
    }

    public void delete(int scooterId) {
        postgresDao.delete(scooterId);
    }

    public List<Scooter> findAllInZone(Polygon polygon) {
        return postgresDao.findAllInZone(polygon);
    }

    public List<Scooter> findAllNearby(GeoSearchParams params) {
        return postgresDao.findAllNearby(params);
    }

    public List<Scooter> batchFindByStatus(ScooterStatus status, int batchSize, int offset) {
        return postgresDao.batchFindByStatus(status, batchSize, offset);
    }

    public List<Scooter> findByIds(List<Integer> scooterIds) {
        return postgresDao.findByIds(scooterIds);
    }
 }
