package com.chump.emulator;

import com.chump.emulator.command.LockCommand;
import com.chump.emulator.command.RechargeCommand;
import com.chump.emulator.command.ScooterCommand;
import com.chump.emulator.command.UnlockCommand;
import com.chump.emulator.event.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class ScooterEmulator {

    private final Integer scooterId;
    private final KafkaTemplate<String, Object> fastKafkaTemplate;
    private final KafkaTemplate<String, Object> reliableKafkaTemplate;

    private final static Duration MAX_COMMAND_AGE = Duration.ofMinutes(2);
    private final static Logger logger = LoggerFactory.getLogger(ScooterEmulator.class);
    private final static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private volatile int battery = 100;
    private volatile Point curLocation = geometryFactory.createPoint(new Coordinate(37.609913, 55.7614868));

    private volatile ScooterStatus status = ScooterStatus.LOCKED;

    public ScooterEmulator(@Value("${scooter.id}") Integer scooterId,
                           @Qualifier("fastKafkaTemplate") KafkaTemplate<String, Object> fastKafkaTemplate,
                           @Qualifier("reliableKafkaTemplate") KafkaTemplate<String, Object> reliableKafkaTemplate) {
        this.scooterId = scooterId;
        this.fastKafkaTemplate = fastKafkaTemplate;
        this.reliableKafkaTemplate = reliableKafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000L)
    public void sendTelemetry() {
        fastKafkaTemplate.send("scooter.telemetry", scooterId.toString(), TelemetryEvent
                .builder()
                .scooterId(scooterId)
                .location(curLocation)
                .battery(battery)
                .build()
        );
    }

    @Scheduled(fixedDelay = 5000L)
    public void sendWaypoint() {
        if (status != ScooterStatus.UNLOCKED) return;

        reliableKafkaTemplate.send("scooter.waypoints", scooterId.toString(), WaypointEvent
                .builder()
                .location(curLocation)
        ).whenComplete(
            (result, ex) -> {
                if (ex != null) {
                    logger.error("Failed to send waypoint for scooter with id: {}", scooterId);
                }
            }
        );
    }

    @KafkaListener(
            topics = "platform.scooter-commands",
            containerFactory = "listenerFactory",
            groupId = "scooter-emulator-${scooter.id}"
    )
    public void commandListener(ScooterCommand command, Acknowledgment ack) {
//  TODO       ack.acknowledge();  Не нужен, т.к. autocommit // Сразу осуществляем сдвиг, команда либо отработала, либо нет, других вариантов нет.

        if (!(command.scooterId() == scooterId)) return;
        if (command.issuedAt().isBefore(Instant.now().minus(MAX_COMMAND_AGE))) {
            logger.warn("Stale command ignored, scooterId: {}, issuedAt: {}", scooterId, command.issuedAt());
            return;
        }

        if (command instanceof LockCommand) {
            if (status == ScooterStatus.LOCKED) {
                logger.debug("Already locked, duplicate command ignored");
            }
            // TODO отправка Locked
            sendStatusEvent(new LockedEvent(scooterId));
            status = ScooterStatus.LOCKED;
        } else if (command instanceof UnlockCommand) {
            if (status == ScooterStatus.UNLOCKED) {
                logger.debug("Already unlocked, duplicate command ignored");
            }
            // TODO отправка Unlocked
            sendStatusEvent(new UnlockedEvent(scooterId));
            status = ScooterStatus.UNLOCKED;
        } else if (command instanceof RechargeCommand) {
            if (battery == 100) {
                logger.debug("Already charged, command ignored");
            }
            battery = 100;
        } else {
            logger.warn("Unknown command type: {}", command.getClass().getSimpleName());
        }
    }

    private void sendStatusEvent(StatusEvent event) {
        reliableKafkaTemplate.send(
                "scooter.status",
                scooterId.toString(),
                event
        ).whenComplete(
                (result, ex) -> {
                    if (ex != null) {
                        logger.error("Failed to send status event for scooter with id: {}", scooterId);
                    }
                }
        );
    }
}
