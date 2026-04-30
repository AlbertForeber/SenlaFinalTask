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

    // Храним не более 8 часов (т.к. современные самокаты в среднем держат заряд не больше 3 часов)
    private static final long WAYPOINT_TTL = 28800L;
    private static final String KEY_PATTERN = "scooter:%d:waypoints";

    public void save(WaypointEntry entry) {
        log.info("Saved waypoint {}", entry); // TODO убрать
        try {
            String key = key(entry.getScooterId());
            redis.rpush(key, GeoConverter.waypointToString(entry));
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
                            return GeoConverter.stringToWaypoint(scooterId, o);
                        } catch (Exception e) {
                            log.warn("One of waypoints failed to parse: {}", o);
                            return null; // TODO (или стоит поменять?) Неудачные точки просто пропускаем
                        }
                    }
            ).filter(Objects::nonNull).toList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to pop waypoints for scooter with id: " + scooterId, e);
        }
    }

    public void clearWaypoints(int scooterId) {
        try {
            String key = key(scooterId);
            redis.del(key);
        } catch (Exception e) {
            throw new DataManipulationException();
        }
    }

    private String key(int tripId) {
        return String.format(KEY_PATTERN, tripId);
    }
}
