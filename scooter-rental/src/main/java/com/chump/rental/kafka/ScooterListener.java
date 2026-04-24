package com.chump.rental.kafka;

import com.chump.rental.kafka.event.StatusEvent;
import com.chump.rental.kafka.event.TelemetryEvent;
import com.chump.rental.kafka.event.WaypointEvent;
import com.chump.rental.service.ScooterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

// TODO Listener находятся на том же слое, что и контроллеры
@Component
public class ScooterListener {

    private final ScooterService scooterService;

    private final static Logger logger = LoggerFactory.getLogger(ScooterListener.class);

    public ScooterListener(ScooterService scooterService) {
        this.scooterService = scooterService;
    }

    @KafkaListener(
            topics = "scooter.telemetry",
            containerFactory = "fastListenerFactory"
    )
    public void telemetryListener(TelemetryEvent event) {
        // TODO сохранение в REDIS
        logger.info(event.toString());
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
            // TODO сохранение в REDIS
            logger.info(event.toString());
            ack.acknowledge();
        } catch (Exception e) {
            // Отрабатывает Error Handler
            logger.error("Failed to process waypoint from scooter with id: {}. Partition: {}, offset: {}",
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
            logger.error("Failed to process status for scooter with id: {}. Partition: {}, offset: {}",
                    event.scooterId(), partition, offset
            );
        }
    }
}
