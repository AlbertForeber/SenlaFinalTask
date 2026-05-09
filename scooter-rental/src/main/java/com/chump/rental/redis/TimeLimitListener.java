package com.chump.rental.redis;

import com.chump.common.config.redis.KeyspaceEventListener;
import com.chump.rental.service.RentalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeLimitListener implements KeyspaceEventListener {

    private static final Pattern KEY_PATTERN = Pattern.compile("trip:(\\d+):time_limit");
    private final RentalService rentalService;

    @Override
    public void onExpired(String expiredKey) {
        Matcher matcher = KEY_PATTERN.matcher(expiredKey);
        if (!matcher.matches()) return;

        try {
            rentalService.handleTimeLimit(Integer.parseInt(matcher.group(1)));
        } catch (NumberFormatException e) {
            log.error("Expired time limit key: {} is malformed", expiredKey);
        } catch (Exception e) {
            log.error("Failed to handle time limit for trip with id: {}", matcher.group(1), e);
        }
    }
}
