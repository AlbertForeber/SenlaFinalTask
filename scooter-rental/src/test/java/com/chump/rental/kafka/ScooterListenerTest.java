package com.chump.rental.kafka;

import com.chump.common.config.kafka.KafkaConfig;
import com.chump.common.kafka.AbstractKafkaTest;
import com.chump.common.kafka.KafkaTestHelper;
import com.chump.rental.dao.ScooterTelemetryRedisDao;
import com.chump.rental.dao.ScooterWaypointRedisDao;
import com.chump.rental.kafka.event.TelemetryEvent;
import com.chump.rental.kafka.event.UnlockedEvent;
import com.chump.rental.kafka.event.WaypointEvent;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.service.ScooterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.context.ContextConfiguration;

import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Scooter listener testing")
@ContextConfiguration(classes = {
        ScooterListener.class,
        ScooterListenerTest.Config.class
})
public class ScooterListenerTest extends AbstractKafkaTest {

    @Autowired
    private ScooterWaypointRedisDao scooterWaypointRedisDao;

    @Autowired
    private ScooterTelemetryRedisDao scooterTelemetryRedisDao;

    @Autowired
    private ScooterService scooterService;


    @Configuration
    @EnableKafka
    public static class Config {

        @Bean
        public ScooterService scooterService() {
            return Mockito.mock(ScooterService.class);
        }

        @Bean
        public ScooterTelemetryRedisDao scooterTelemetryRedisDao() {
            return Mockito.mock(ScooterTelemetryRedisDao.class);
        }

        @Bean
        public ScooterMapper scooterMapper() {
            return Mockito.mock(ScooterMapper.class);
        }

        @Bean
        public ScooterWaypointRedisDao scooterWaypointRedisDao() {
            return Mockito.mock(ScooterWaypointRedisDao.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return ScooterListenerTest.objectMapper();
        }

        @Bean
        @Primary
        public ConsumerFactory<String, Object> reliableConsumerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.chump.*");
            return new DefaultKafkaConsumerFactory<>(
                    props,
                    new StringDeserializer(),
                    new JsonDeserializer<>(objectMapper())
            );
        }

        @Bean
        public ConsumerFactory<String, Object> fastConsumerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.chump.*");
            return new DefaultKafkaConsumerFactory<>(
                    props,
                    new StringDeserializer(),
                    new JsonDeserializer<>(objectMapper())
            );
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, Object> reliableListenerFactory(
                ConsumerFactory<String, Object> reliableConsumerFactory) {
            var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
            factory.setConsumerFactory(reliableConsumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
            factory.setConcurrency(1); // в тестах параллелизм не нужен
            factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) ->
                    System.out.println("ERROR: " + exception.getMessage()),
                    new FixedBackOff(0L, 0)));
            return factory;
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, Object> fastListenerFactory(
                ConsumerFactory<String, Object> reliableConsumerFactory) {
            var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
            factory.setConsumerFactory(reliableConsumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
            factory.setConcurrency(1); // в тестах параллелизм не нужен
            factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) ->
                    System.out.println("ERROR: " + exception.getMessage()),
                    new FixedBackOff(0L, 0)));
            return factory;
        }
    }

    @Test
    @DisplayName("Waypoint listener should save received waypoint to redis")
    public void waypointListenerShouldSaveWaypoint() throws Exception {
        try (KafkaTestHelper helper = kafkaTestHelper()) {
            helper.send("scooter.waypoints", "1", new WaypointEvent());
            verify(scooterWaypointRedisDao, timeout(10000).times(1)).save(any());
        }
    }

    @Test
    @DisplayName("Telemetry listener should save received telemetry to redis")
    public void telemetryListenerShouldSaveTelemetry() throws Exception {
        try (KafkaTestHelper helper = kafkaTestHelper()) {
            helper.send("scooter.telemetry", "1", new TelemetryEvent());
            verify(scooterTelemetryRedisDao, timeout(10000).times(1)).save(anyInt(), any());
        }
    }

    @Test
    @DisplayName("Status listener should update received status")
    public void statusListenerShouldUpdateStatus() throws Exception {
        try (KafkaTestHelper helper = kafkaTestHelper()) {
            helper.send("scooter.status", "1", new UnlockedEvent());
            verify(scooterService, timeout(10000).times(1)).updateReceivedStatus(anyInt());
        }
    }
}
