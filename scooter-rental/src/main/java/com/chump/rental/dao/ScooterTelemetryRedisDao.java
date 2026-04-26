package com.chump.rental.dao;

import com.chump.common.exception.DataManipulationException;
import com.chump.rental.dto.entry.TelemetryEntry;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanIterator;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// HGETALL, допустимо, т.к. количество полей мало
@Component
public class ScooterTelemetryRedisDao {

    private final RedisCommands<String, String> redis;

    private final static long TELEMETRY_TTL = 300L;
    private final static String KEY_PATTERN = "scooter:%s:telemetry";

    public ScooterTelemetryRedisDao(RedisCommands<String, String> redis) {
        this.redis = redis;
    }

    public void save(int scooterId, TelemetryEntry entry) {
        try {
            String key = key(scooterId);
            redis.hset(key, Map.of(
                    "longitude", String.valueOf(entry.getLongitude()),
                    "latitude", String.valueOf(entry.getLatitude()),
                    "battery", String.valueOf(entry.getBattery()),
                    "updatedAt", Instant.now().toString()
            ));
            redis.expire(key, TELEMETRY_TTL);
        } catch (Exception e) {
            throw new DataManipulationException(
                    "Failed to save telemetry for scooter with id: " + scooterId, e
            );
        }
    }

    public TelemetryEntry findById(int scooterId) {
        try {
            return buildTelemetryEntry(scooterId);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to save telemetry for scooter with id: " + scooterId, e);
        }
    }

    public List<TelemetryEntry> findBatch(int batchSize) {
        try {
            List<TelemetryEntry> result = new ArrayList<>();

            // ScanIterator автоматизирует работу с пачками данных, отсылаемых SCAN
            ScanIterator<String> batch = ScanIterator.scan(redis, ScanArgs.Builder
                    .matches(String.format(KEY_PATTERN, "*"))
                    .limit(batchSize)
            );

            while (batch.hasNext()) {
                int scooterId = getScooterIdFromKey(batch.next());
                result.add(buildTelemetryEntry(scooterId));
            }

            return result;
        } catch (Exception e) {
            throw new DataManipulationException(
                    "Failed to find batch of scooters' telemetries",
                    e
            );
        }
    }

    public void deleteByScooterIds(List<Integer> scooterIds) {
        try {
            redis.unlink(scooterIds.stream().map(this::key).toArray(String[]::new));
        } catch (Exception e) {
            throw new DataManipulationException(
                    "Failed to delete telemetry for scooter with ids: " + scooterIds,
                    e);
        }
    }

    private String key(Integer scooterId) {
        return String.format(KEY_PATTERN, scooterId.toString());
    }

    private TelemetryEntry buildTelemetryEntry(int scooterId) {
        Map<String, String> fields = redis.hgetall(key(scooterId));
        try {
            return TelemetryEntry.builder()
                    .scooterId(scooterId)
                    .battery(Integer.parseInt(fields.get("battery")))
                    .latitude(Float.parseFloat(fields.get("latitude")))
                    .longitude(Float.parseFloat(fields.get("longitude")))
                    .build();
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Telemetry entry is malformed for scooter with id: " + scooterId);
        }
    }

    private int getScooterIdFromKey(String key) {
        try {
            return Integer.parseInt(key.split(":")[1]);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Telemetry key is malformed: " + key);
        }
    }
}
