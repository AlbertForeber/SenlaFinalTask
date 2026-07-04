package com.chump.common.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;

@Tag("integration")
@Tag("container")
@ExtendWith(SpringExtension.class)
public class AbstractKafkaTest {

    private static final KafkaContainer kafkaContainer =
            KafkaTestContainer.getInstance(); // Инициализация контейнера до контекста Spring

    protected static final String BOOTSTRAP_SERVERS =
            kafkaContainer.getBootstrapServers();

    protected static ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule()) // Для поддержки Instant, LocalDate
                .addModule(new JtsModule()) // Для поддержки Point, LineString, Polygon
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Instant как даты, а не как числа
                .build();
    }

    protected KafkaTestHelper kafkaTestHelper() {
        return new KafkaTestHelper(BOOTSTRAP_SERVERS, AbstractKafkaTest.objectMapper());
    }
}
