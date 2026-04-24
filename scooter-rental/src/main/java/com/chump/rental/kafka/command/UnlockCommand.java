package com.chump.rental.kafka.command;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public final class UnlockCommand implements ScooterCommand {

    private int scooterId;
    private Instant issuedAt;

    @Override
    public int scooterId() {
        return scooterId;
    }

    @Override
    public Instant issuedAt() {
        return issuedAt;
    }
}
