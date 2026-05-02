package com.chump.common.config.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@DependsOn("liquibase")
public class HibernateConfig {

    @Value("${hibernate.jdbc.batch-size:50}")
    private int batchSize;

    @Bean
    public LocalSessionFactoryBean sessionFactory(
            DataSource dataSource
    ) {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();

        factory.setDataSource(dataSource);
        factory.setPackagesToScan(
                "com.chump.rental.model",
                "com.chump.user.model",
                "com.chump.billing.model",
                "com.chump.auth.model"
        );
        factory.setHibernateProperties(hibernateProperties());

        return factory;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();

        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.use_sql_comments", true);
        properties.put("hibernate.jdbc.batch_size", batchSize);
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        // TODO в проде убрать:
        properties.put("hibernate.generate_statistics", true);
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);

        return properties;
    }
}
