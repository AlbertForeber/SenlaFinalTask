package com.chump.rental.dao;

import com.chump.common.exception.DataManipulationException;
import com.chump.rental.dto.entry.WaypointEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripWaypointRedisDao {

    private final RedisCommands<String, String> redis;
    private final ObjectMapper mapper;

    // Храним не более 8 часов (т.к. современные самокаты в среднем держат заряд не больше 3 часов)
    private static final long WAYPOINT_TTL = 28800L;
    private static final String KEY_PATTERN = "trip:%d:waypoints";

    public void save(int tripId, WaypointEntry entry) {
        try {
            redis.rpush(key(tripId), mapper.writeValueAsString(entry));
        } catch (Exception e) {
            throw new DataManipulationException("Failed to save trip waypoints with id: " + tripId, e);
        }
    }

    private List<WaypointEntry> batchPopWaypoints(int tripId, int batchSize) {
        try {
            String key = key(tripId);
            List<String> result = redis.rpop(key, redis.llen(key));

            return result.stream().map(
                    o -> {
                        try {
                            return mapper.readValue(o, WaypointEntry.class);
                        } catch (JsonProcessingException e) {
                            log.error("Failed to parse JSON: \"{}\"", o);
                            return null;
                        }
                    }
            ).filter(Objects::nonNull).toList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to pop waypoints for trip with id: " + tripId, e);
        }
    }

    private String key(int tripId) {
        return String.format(KEY_PATTERN, tripId);
    }
}
