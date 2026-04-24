package com.chump.emulator.command;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public final class LockCommand implements ScooterCommand {

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
