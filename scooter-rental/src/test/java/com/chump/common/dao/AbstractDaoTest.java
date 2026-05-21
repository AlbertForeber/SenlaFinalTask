package com.chump.common.dao;

import com.chump.common.config.DataSourceTestConfig;
import com.chump.common.config.HibernateTestConfig;
import com.chump.common.config.db.LiquibaseConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@Tag("integration")
@Tag("container")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        DataSourceTestConfig.class,
        LiquibaseConfig.class,
        HibernateTestConfig.class
})
@Transactional
public abstract class AbstractDaoTest {

    private static final PostgreSQLContainer<?> POSTGRES =
            PostgresTestContainer.getInstance(); // Инициализация контейнера до контекста Spring

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    // Для очистки кэша первого уровня
    protected void flushAndClear() {
        getCurrentSession().flush();
        getCurrentSession().clear();
    }
}
