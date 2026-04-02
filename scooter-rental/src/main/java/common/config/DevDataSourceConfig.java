package common.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:db-dev.properties")
@Profile("dev")
public class DevDataSourceConfig {
        @Value("db.url") String url;
        @Value("db.username") String username;
        @Value("db.password") String password;
        @Value("db.pool.max-size") Integer poolMaxSize;
        @Value("db.pool.min-idle") Integer poolMinIdle;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolMaxSize);
        config.setMinimumIdle(poolMinIdle);
        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }
}
