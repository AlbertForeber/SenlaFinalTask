package com.chump.rental.kafka;

import com.chump.rental.kafka.command.LockCommand;
import com.chump.rental.kafka.command.RechargeCommand;
import com.chump.rental.kafka.command.UnlockCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ScooterProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendLock(Integer scooterId) {
        kafkaTemplate.send(
                "platform.scooter-commands",
                scooterId.toString(),
                new LockCommand(scooterId, Instant.now())
        );
    }

    public void sendUnlock(Integer scooterId) {
        kafkaTemplate.send(
                "platform.scooter-commands",
                scooterId.toString(),
                new UnlockCommand(scooterId, Instant.now())
        );
    }

    public void sendRecharge(Integer scooterId) {
        kafkaTemplate.send(
                "platform.scooter-commands",
                scooterId.toString(),
                new RechargeCommand(scooterId, Instant.now())
        );
    }
}
