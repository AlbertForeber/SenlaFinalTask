package com.chump.common.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceTestConfig {

    @Bean
    public DataSource dataSource(
            @Value("${test.db.username}") String username,
            @Value("${test.db.password}") String password,
            @Value("${test.db.url}") String url
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);

        return new HikariDataSource(config);
    }
}
