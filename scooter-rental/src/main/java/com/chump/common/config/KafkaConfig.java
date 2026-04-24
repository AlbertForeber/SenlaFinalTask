package com.chump.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ObjectMapper objectMapper;

    public KafkaConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> baseProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "platform-group");
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "com.chump.*");
        properties.put(JsonDeserializer.TYPE_MAPPINGS,
                "com.chump.emulator.event.TelemetryEvent:com.chump.rental.kafka.event.TelemetryEvent," +
                "com.chump.emulator.event.WaypointEvent:com.chump.rental.kafka.event.WaypointEvent," +
                "com.chump.emulator.event.LockedEvent:com.chump.rental.kafka.event.LockedEvent," +
                "com.chump.emulator.event.UnlockedEvent:com.chump.rental.kafka.event.UnlockedEvent"
        );

        return properties;
    }

    @Bean
    public ConsumerFactory<String, Object> fastConsumerFactory() {
        Map<String, Object> properties = baseProperties();
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000);

        // Старую телеметрию не читаем
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                new JsonDeserializer<>(objectMapper)
        );
    }

    @Bean
    public ConsumerFactory<String, Object> reliableConsumerFactory() {
        Map<String, Object> properties = baseProperties();
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // Читаем с первого сообщение если offset не найден
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                new JsonDeserializer<>(objectMapper)
        );
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        // At least once настройки для отправки системой изменений самокату
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new DefaultKafkaProducerFactory<>(
                properties,
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> fastListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        // Default-параметры AckMode + Auto-commit
        factory.setConsumerFactory(fastConsumerFactory());
        factory.setConcurrency(3);
        // Обработка ошибок не требуется (default: 10 retries, 0 ms), потеря телеметрии некритична

        return  factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> reliableListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(reliableConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(3); // Количество партиций, оптимально для параллелизма
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate()));

        return factory;
    }

    private DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        var backOff = new FixedBackOff(2000L, 3);
        var handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(DeserializationException.class);

        return handler;
    }
}
