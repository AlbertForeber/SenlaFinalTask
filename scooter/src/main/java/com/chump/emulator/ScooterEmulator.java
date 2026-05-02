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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ScooterEmulator {

    private final Integer scooterId;
    private final KafkaTemplate<String, Object> fastKafkaTemplate;
    private final KafkaTemplate<String, Object> reliableKafkaTemplate;

    private static final Duration MAX_COMMAND_AGE = Duration.ofMinutes(2);
    private static final Logger logger = LoggerFactory.getLogger(ScooterEmulator.class);
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private final AtomicInteger battery = new AtomicInteger(100);
    private volatile double latitude = 0;
    private volatile double longitude = 0;
    private final Stack<Map.Entry<Double, Double>> waypoints = new Stack<>();

    private volatile ScooterStatus status = ScooterStatus.LOCKED;

    public ScooterEmulator(@Value("${scooter.id}") Integer scooterId,
                           @Qualifier("fastKafkaTemplate") KafkaTemplate<String, Object> fastKafkaTemplate,
                           @Qualifier("reliableKafkaTemplate") KafkaTemplate<String, Object> reliableKafkaTemplate) {
        this.scooterId = scooterId;
        this.fastKafkaTemplate = fastKafkaTemplate;
        this.reliableKafkaTemplate = reliableKafkaTemplate;
        waypoints.addAll(List.of(
                Map.entry(37.6099130, 55.7614868),
                Map.entry(37.6095271, 55.7619315),
                Map.entry(37.6101762, 55.7621156),
                Map.entry(37.6108360, 55.7623329),
                Map.entry(37.6113403, 55.7625079),
                Map.entry(37.6118660, 55.7626679),
                Map.entry(37.6123971, 55.7628339),
                Map.entry(37.6128799, 55.7629576),
                Map.entry(37.6127189, 55.7631357),
                Map.entry(37.6124668, 55.7634013),
                Map.entry(37.6122093, 55.7636397),
                Map.entry(37.6120377, 55.7638570),
                Map.entry(37.6117641, 55.7641920),
                Map.entry(37.6114690, 55.7645180),
                Map.entry(37.6111847, 55.7648198),
                Map.entry(37.6107019, 55.7655713),
                Map.entry(37.6101923, 55.7659606),
                Map.entry(37.6098329, 55.7663287),
                Map.entry(37.6096478, 55.7665113),
                Map.entry(37.6093233, 55.7667452),
                Map.entry(37.6091033, 55.7669414),
                Map.entry(37.6090014, 55.7671451),
                Map.entry(37.6089129, 55.7672416),
                Map.entry(37.6090577, 55.7673065),
                Map.entry(37.6094171, 55.7674423)
        ));
    }

    @Scheduled(fixedDelay = 10000L)
    public void drainBattery() {
        if (battery.get() > 0) battery.decrementAndGet();
    }

    @Scheduled(fixedDelay = 5000L)
    public void sendTelemetry() {
        fastKafkaTemplate.send("scooter.telemetry", scooterId.toString(), TelemetryEvent
                .builder()
                .scooterId(scooterId)
                .location(location())
                .battery(battery.get())
                .build()
        );
    }

    // ~раз в 9 метров
    @Scheduled(fixedDelay = 10000L)
    public void sendWaypoint() {
        if (status != ScooterStatus.UNLOCKED) return;

        emulateMove();
        reliableKafkaTemplate.send("scooter.waypoints", scooterId.toString(), WaypointEvent.builder()
                        .location(location())
                        .scooterId(scooterId)
                        .sendAt(Instant.now())
                        .build()
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
    public void commandListener(ScooterCommand command) {

        if (!(command.scooterId() == scooterId)) return;
        if (command.issuedAt().isBefore(Instant.now().minus(MAX_COMMAND_AGE))) {
            // TODO а как же компенсирующие команды?
            logger.warn("Stale command ignored, scooterId: {}, issuedAt: {}", scooterId, command.issuedAt());
            return;
        }

        if (command instanceof LockCommand) {
            if (status == ScooterStatus.LOCKED) {
                logger.debug("Already locked, duplicate command ignored");
            }
            // TODO поменять лог
            logger.info("Received lock command");
            sendStatusEvent(new LockedEvent(scooterId));
            // sendWaypoint(); // TODO Отправляем последнюю точку - точку остановки.
            status = ScooterStatus.LOCKED;
        } else if (command instanceof UnlockCommand) {
            if (status == ScooterStatus.UNLOCKED) {
                logger.debug("Already unlocked, duplicate command ignored");
            }
            // TODO поменять лог
            logger.info("Received unlock command");
            sendStatusEvent(new UnlockedEvent(scooterId));
            status = ScooterStatus.UNLOCKED;
        } else if (command instanceof RechargeCommand) {
            if (battery.get() == 100) {
                logger.debug("Already charged, command ignored");
            }
            // TODO поменять лог
            logger.info("Received recharge command");
            battery.set(100);
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

    private Point location() {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private void emulateMove() {
        if (!waypoints.isEmpty()) {
            Map.Entry<Double, Double> waypoint = waypoints.pop();
            longitude = waypoint.getKey();
            latitude = waypoint.getValue();
        }
    }
}
