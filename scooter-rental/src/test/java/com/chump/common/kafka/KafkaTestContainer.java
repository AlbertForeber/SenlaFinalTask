package com.chump.common.kafka;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaTestContainer {

    private static final KafkaContainer INSTANCE = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    static {
        INSTANCE.start();
        System.setProperty("kafka.bootstrap-servers", INSTANCE.getBootstrapServers());
    }

    private KafkaTestContainer() {
    }

    public static KafkaContainer getInstance() {
        return INSTANCE;
    }
}
