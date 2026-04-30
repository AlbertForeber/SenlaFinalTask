package com.chump.rental.kafka;

import com.chump.rental.dao.ScooterTelemetryRedisDao;
import com.chump.rental.dao.ScooterWaypointRedisDao;
import com.chump.rental.kafka.event.StatusEvent;
import com.chump.rental.kafka.event.TelemetryEvent;
import com.chump.rental.kafka.event.WaypointEvent;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.service.ScooterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

// TODO Listener находятся на том же слое, что и контроллеры
@Component
@RequiredArgsConstructor
@Slf4j
public class ScooterListener {

    private final ScooterService scooterService;
    private final ScooterTelemetryRedisDao scooterTelemetryRedisDao;
    private final ScooterMapper scooterMapper;
    private final ScooterWaypointRedisDao scooterWaypointRedisDao;

    @KafkaListener(
            topics = "scooter.telemetry",
            containerFactory = "fastListenerFactory"
    )
    public void telemetryListener(TelemetryEvent event) {
        // TODO сохранение в REDIS - ОБРАБОТКА ОШИБОК (не нужна, т.к. телеметрия)
        scooterTelemetryRedisDao.save(event.getScooterId(), scooterMapper.toTelemetryEntry(event));
    }

    @KafkaListener(
            topics = "scooter.waypoints",
            containerFactory = "reliableListenerFactory"
    )
    public void waypointListener(
            @Payload WaypointEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) int offset,
            Acknowledgment ack
    ) {
        try {
            log.info(event.toString()); // TODO убрать
            scooterWaypointRedisDao.save(scooterMapper.toWaypointEntry(event)); // TODO сохранение в REDIS
            ack.acknowledge();
        } catch (Exception e) {
            // Отрабатывает Error Handler
            log.error("Failed to process waypoint from scooter with id: {}. Partition: {}, offset: {}",
                    event.getScooterId(), partition, offset);
        }
    }

    @KafkaListener(
            topics = "scooter.status",
            containerFactory = "reliableListenerFactory"
    )
    public void statusListener(
            @Payload StatusEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) int offset,
            Acknowledgment ack
    ) {
        try {
            scooterService.updateReceivedStatus(event.scooterId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process status for scooter with id: {}. Partition: {}, offset: {}",
                    event.scooterId(), partition, offset
            );
        }
    }
}