package com.chump.rental.kafka.event;

public sealed interface StatusEvent permits LockedEvent, UnlockedEvent {

    int scooterId();
}
