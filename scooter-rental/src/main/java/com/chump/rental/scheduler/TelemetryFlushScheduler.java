package com.chump.rental.scheduler;

import com.chump.rental.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelemetryFlushScheduler {

    private final TelemetryService telemetryService;

    // Слив телеметрии в БД раз в минуту
    @Scheduled(initialDelay = 5000L, fixedDelay = 10000L)
    @SchedulerLock(name = "telemetry-per-minute", lockAtLeastFor = "PT10S", lockAtMostFor = "PT2M")
    public void flushTelemetry() {
        telemetryService.processTelemetryUpdate();
    }
}
