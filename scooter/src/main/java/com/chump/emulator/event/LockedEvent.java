package com.chump.emulator.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class LockedEvent implements StatusEvent {

    private int scooterId;

    @Override
    public int scooterId() {
        return scooterId;
    }
}
