package com.chump.emulator.command;
import java.time.Instant;

public sealed interface ScooterCommand permits LockCommand, RechargeCommand, UnlockCommand {

    int scooterId();
    Instant issuedAt();
}
