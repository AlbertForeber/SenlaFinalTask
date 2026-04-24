package com.chump.emulator.event;

public sealed interface StatusEvent permits LockedEvent, UnlockedEvent {

    int scooterId();
}
