package com.chump.common.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class KafkaTestHelper implements AutoCloseable {

    private final KafkaProducer<String, Object> producer;
    private final ObjectMapper objectMapper;

    public KafkaTestHelper(String bootstrapServer, @NotNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.producer = buildProducer(bootstrapServer);
    }

    private KafkaProducer<String, Object> buildProducer(String bootstrapServers) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new KafkaProducer<>(
                properties,
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
    }

    public void send(String topic, String key, Object value) throws Exception {
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                topic,
                key,
                value
        );
        record.headers().add(
                "__TypeId__",
                value.getClass().getName().getBytes(StandardCharsets.UTF_8)
        );
        producer.send(record).get();
    }

    @Override
    public void close() {
        producer.close();
    }
}