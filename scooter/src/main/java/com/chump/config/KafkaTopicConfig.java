package com.chump.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.time.Duration;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${kafka.bootstrap-servers}") String servers) {
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers));
    }

    @Bean
    public NewTopic telemetryTopic() {
        return TopicBuilder.name("scooter.telemetry")
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(1).toMillis()))
                .build();
    }

    @Bean
    public NewTopic waypointTopic() {
        return TopicBuilder.name("scooter.waypoints")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic scooterCommandTopic() {
        return TopicBuilder.name("platform.scooter-commands")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic scooterStatus() {
        return TopicBuilder.name("scooter.status")
                .partitions(3)
                .replicas(1)
                .build();
    }
}