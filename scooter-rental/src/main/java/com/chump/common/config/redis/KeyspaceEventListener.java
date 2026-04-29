package com.chump.common.config.redis;

public interface KeyspaceEventListener {

    void onExpired(String expiredKey);
}
