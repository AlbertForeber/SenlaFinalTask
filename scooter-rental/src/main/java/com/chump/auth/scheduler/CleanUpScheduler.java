package com.chump.auth.scheduler;

import com.chump.auth.service.SessionService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleanUpScheduler {

    private final SessionService sessionService;

    // TODO Изменить rate
    @Scheduled(fixedRate = 5000L)
    @SchedulerLock(lockAtLeastFor = "PT1M", lockAtMostFor = "PT2H")
    public void cleanUpStaleRefreshTokens() {
        sessionService.cleanUpStaleSessions();
    }
}
