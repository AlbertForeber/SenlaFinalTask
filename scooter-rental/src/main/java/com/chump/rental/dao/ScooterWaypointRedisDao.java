package com.chump.rental.dao;

import com.chump.common.exception.DataManipulationException;
import com.chump.common.utils.GeoConverter;
import com.chump.rental.dto.entry.WaypointEntry;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScooterWaypointRedisDao {

    private final RedisCommands<String, String> redis;
    private final GeoConverter geoConverter;

    // Храним не более 8 часов (т.к. современные самокаты в среднем держат заряд не больше 3 часов)
    private static final long WAYPOINT_TTL = 28800L;
    private static final String KEY_PATTERN = "scooter:%d:waypoints";

    public void save(WaypointEntry entry) {
        try {
            String key = key(entry.getScooterId());
            redis.rpush(key, geoConverter.waypointToString(entry));
            redis.expire(key, WAYPOINT_TTL);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to save trip waypoints with id: " + entry.getScooterId(), e);
        }
    }

    public List<WaypointEntry> getWaypoints(int scooterId) {
        try {
            String key = key(scooterId);
            List<String> result = redis.lrange(key, 0, -1);

            return result.stream().map(
                    o -> {
                        try {
                            return geoConverter.stringToWaypoint(scooterId, o);
                        } catch (Exception e) {
                            log.warn("One of waypoints failed to parse: {}", o);
                            return null;
                        }
                    }
            ).filter(Objects::nonNull).toList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to get waypoints for scooter with id: " + scooterId, e);
        }
    }

    public void clearWaypoints(int scooterId) {
        try {
            redis.del(key(scooterId));
        } catch (Exception e) {
            throw new DataManipulationException("Failed to clear waypoints for scooters with id: " + scooterId, e);
        }
    }

    private String key(int tripId) {
        return String.format(KEY_PATTERN, tripId);
    }
}
