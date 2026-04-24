package com.chump.rental.kafka;

import com.chump.rental.kafka.command.LockCommand;
import com.chump.rental.kafka.command.UnlockCommand;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScooterProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ScooterProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLock(Integer scooterId) {
        kafkaTemplate.send(
                "platform.scooter-commands",
                scooterId.toString(),
                new LockCommand());
    }

    public void sendUnlock(Integer scooterId) {
        kafkaTemplate.send(
                "platform.scooter-commands",
                scooterId.toString(),
                new UnlockCommand());
    }
}
