package com.chump.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@DependsOn("liquibase")
public class HibernateConfig {

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
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "true");

        return properties;
    }
}
