package com.chump.common.config.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port:6379}")
    private int port;

    @Value("${redis.password:}")
    private String password;

    @Bean(destroyMethod = "close")
    public RedisClient redisClient() {
        RedisURI redisURI = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withTimeout(Duration.ofSeconds(5))
                .withPassword(password == null ? null : password.toCharArray())
                .build();

        return RedisClient.create(redisURI);
    }

    @Bean(destroyMethod = "close")
    @Qualifier("default")
    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        return redisClient.connect();
    }

    @Bean
    public RedisCommands<String, String> redisCommand(
            @Qualifier("default") StatefulRedisConnection<String, String> connection
    ) {
        return connection.sync();
    }

    @Bean(destroyMethod = "close")
    @Qualifier("pubSub")
    public StatefulRedisPubSubConnection<String, String> pubSubConnection(
            RedisClient redisClient
    ) {
        return redisClient.connectPubSub();
    }
}
