package com.chump.common.config.redis;

import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
// Bean инициализируется лениво, InitializingBean берет на себя ответственность
// одноразовой отработки для подписки
public class RedisKeyspaceSubscriber implements InitializingBean {

    private final StatefulRedisPubSubConnection<String, String> conn;
    private final List<KeyspaceEventListener> listeners;

    @Override
    public void afterPropertiesSet() {
        conn.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String pattern, String channel, String message) {
                // channel  = "__keyevent@0__:expired"
                // message  = "scooter:7:pending"
                listeners.forEach(l -> l.onExpired(message));
            }
        });

        conn.sync().psubscribe("__keyevent@*__:expired");
    }
}
