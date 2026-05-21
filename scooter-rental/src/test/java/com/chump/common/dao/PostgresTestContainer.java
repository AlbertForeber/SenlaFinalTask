package com.chump.common.dao;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

// Singleton для оптимизации в тестах
public class PostgresTestContainer {

    private static final String IMAGE = "postgis/postgis:16-3.4";

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>(
                DockerImageName
                        .parse(IMAGE)
                        // Чтобы указать, что указанный образ совместим с Postgres
                        .asCompatibleSubstituteFor("postgres")
        )
                .withDatabaseName("scooter_test")
                .withUsername("test")
                .withPassword("test");
    static {
        INSTANCE.start();
        System.setProperty("test.db.url", INSTANCE.getJdbcUrl());
        System.setProperty("test.db.username", INSTANCE.getUsername());
        System.setProperty("test.db.password", INSTANCE.getPassword());
    }

    private PostgresTestContainer() {
    }

    public static PostgreSQLContainer<?> getInstance() {
        return INSTANCE;
    }
}
