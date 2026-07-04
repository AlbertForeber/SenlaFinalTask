package com.chump.common.config.redis;

// TODO переименовать на KeyEvent
public interface KeyspaceEventListener {

    void onExpired(String expiredKey);
}
