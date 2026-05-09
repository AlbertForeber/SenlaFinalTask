package com.chump.rental.dao;

import com.chump.common.exception.DataManipulationException;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Для предотвращения безграничной поездки при поинтервальном тарифе
// (если у пользователя баланс на 10 минут, час ездить он не сможет)
@Component
@RequiredArgsConstructor
public class TripTimeLimitRedisDao {

    private final RedisCommands<String, String> redis;

    private static final String KEY_PATTERN = "trip:%d:time_limit";

    public void setTimeLimit(int tripId, int timeLimitSeconds) {
        try {
            String key = key(tripId);
            redis.set(key, "");
            redis.expire(key, timeLimitSeconds);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to set time limit for trip with id: " + tripId, e);
        }
    }

    public void deleteTimeLimit(int tripId) {
        try {
            redis.del(key(tripId));
        } catch (Exception e) {
            throw new DataManipulationException("Failed to delete time limit for trip with id: " + tripId, e);
        }
    }

    private String key(int tripId) {
        return String.format(KEY_PATTERN, tripId);
    }
}
