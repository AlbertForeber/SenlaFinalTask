package com.chump.rental.repo;

import com.chump.common.dto.param.GeoSearchParams;
import com.chump.common.utils.GeoConverter;
import com.chump.rental.dao.ScooterPostgresDao;
import com.chump.rental.dao.ScooterTelemetryRedisDao;
import com.chump.rental.dto.entry.TelemetryEntry;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.status.ScooterStatus;
import io.lettuce.core.RedisException;
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
    private GeoConverter geoConverter;

    public List<Scooter> findAll() {
        return postgresDao.findAll();
    }

    public Optional<Scooter> findByIdWithActualInfoAndModel(int scooterId) {
        return postgresDao.findByIdWithModel(scooterId)
                .map(o -> {
                    // Актуальная информация важна для двигающихся самокатов
                    if (o.getStatus() == ScooterStatus.OCCUPIED) {
                        try {
                            TelemetryEntry telemetryEntry = redisDao.findById(scooterId);
                            log.info("Got telemetry {}", telemetryEntry);

                            o.setBattery(telemetryEntry.getBattery());
                            o.setLocation(geoConverter.coordsToPoint(
                                    telemetryEntry.getLongitude(),
                                    telemetryEntry.getLatitude()
                            ));
                        } catch (RedisException e) {
                            log.warn("Redis unavailable to refresh telemetry for scooter with id: {}. Skipping",
                                    o.getId());
                        } catch (Exception e) {
                            log.error("Unexpected error when refreshing telemetry for scooter with id: {}",
                                    o.getId(), e);
                        }
                    }
                    return o;
                });
    }

    public Optional<Scooter> findById(int scooterId) {
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

    // TODO убрать
    public List<Scooter> findByIds(List<Integer> scooterIds) {
        return postgresDao.findByIds(scooterIds);
    }
 }
