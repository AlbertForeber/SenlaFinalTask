package com.chump.rental.dao;

import com.chump.common.exception.DataManipulationException;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScooterPendingRedisDao {

    private final RedisCommands<String, String> redis;

    private static final long PENDING_TTL = 15L;
    private static final String KEY_PATTERN = "scooter:%d:pending";

    public void setPending(int scooterId) {
        try {
            String key = key(scooterId);
            redis.set(key, "");
            redis.expire(key, PENDING_TTL);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to set pending for scooter with id: " + scooterId, e);
        }
    }

    public void deletePending(int scooterId) {
        try {
            redis.del(key(scooterId));
        } catch (Exception e) {
            throw new DataManipulationException("Failed to delete pending for scooter with id: " + scooterId, e);
        }
    }

    private String key(int scooterId) {
        return String.format(KEY_PATTERN, scooterId);
    }
}
