package com.chump.common.config;

import com.chump.auth.model.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@DependsOn("liquibase")
public class HibernateTestConfig {

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

        Properties properties = new Properties();
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        factory.setHibernateProperties(properties);

        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            SessionFactory sessionFactory
    ) {
        return new HibernateTransactionManager(sessionFactory);
    }
}
