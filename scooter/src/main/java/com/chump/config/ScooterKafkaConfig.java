package com.chump.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan({"com.chump.emulator", "com.chump.config"})
@EnableScheduling
@EnableKafka
public class ScooterKafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;
    private final ObjectMapper objectMapper;

    public ScooterKafkaConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private Map<String, Object> baseProperties() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return properties;
    }

    // Надежный для точек
    @Bean
    public ProducerFactory<String, Object> reliableProducerFactory() {
        Map<String, Object> properties = baseProperties();

        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // автоматически retries=MAX_INT
        return new DefaultKafkaProducerFactory<>(properties,
                new StringSerializer(),
                new JsonSerializer<>(objectMapper));
    }

    @Bean
    public ProducerFactory<String, Object> fastProducerFactory() {
        Map<String, Object> properties = baseProperties();

        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        properties.put(ProducerConfig.RETRIES_CONFIG, 0);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        return new DefaultKafkaProducerFactory<>(
                properties,
                new StringSerializer(),
                new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, Object> reliableKafkaTemplate() {
        return new KafkaTemplate<>(reliableProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, Object> fastKafkaTemplate() {
        return new KafkaTemplate<>(fastProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true); TODO оставляем дефолтный AckMode.BATCH (коммитит Spring без Kafka)
//        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000L);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "com.chump.*");
        properties.put(JsonDeserializer.TYPE_MAPPINGS,
                "com.chump.rental.kafka.command.LockCommand:com.chump.emulator.command.LockCommand," +
                "com.chump.rental.kafka.command.RechargeCommand:com.chump.emulator.command.RechargeCommand," +
                "com.chump.rental.kafka.command.UnlockCommand:com.chump.emulator.command.UnlockCommand"
        );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                new JsonDeserializer<>(objectMapper)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> listenerFactory() {
        var listenerFactory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        listenerFactory.setConsumerFactory(consumerFactory());
        listenerFactory.setConcurrency(3);
//       TODO listenerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return listenerFactory;
    }

//    TODO private DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
//        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
//        var backOff = new FixedBackOff(2000L, 3);
//        var handler = new DefaultErrorHandler(recoverer, backOff);
//        handler.addNotRetryableExceptions(DeserializationException.class);
//
//        return handler;
//    }
}
