package com.chump.rental.kafka.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public final class UnlockedEvent implements StatusEvent {

    private int scooterId;

    @Override
    public int scooterId() {
        return scooterId;
    }
}
