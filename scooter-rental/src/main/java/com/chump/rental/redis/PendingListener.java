package com.chump.rental.redis;

import com.chump.common.config.redis.KeyspaceEventListener;
import com.chump.rental.service.ScooterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class PendingListener implements KeyspaceEventListener {

    private static final Pattern KEY_PATTERN = Pattern.compile("scooter:(\\d+):pending");
    private final ScooterService scooterService;

    @Override
    public void onExpired(String expiredKey) {
        Matcher matcher = KEY_PATTERN.matcher(expiredKey);
        if (!matcher.matches()) return;

        try {
            scooterService.handleStatusTimeout(Integer.parseInt(matcher.group(1)));
        } catch (NumberFormatException e) {
            log.error("Expired pending key: {} is malformed", expiredKey);
        } catch (Exception e) {
            log.error("Failed to handle status timeout for scooter with id: {}", matcher.group(1), e);
        }
    }
}
