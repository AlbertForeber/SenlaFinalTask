package com.chump.user.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.user.model.Scope;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ScopeDao extends AbstractHibernateDao<Scope, Integer> {

    public ScopeDao(SessionFactory sessionFactory) {
        super(Scope.class, sessionFactory);
    }

    public List<Scope> findByIds(List<Integer> scopeIds) {
        try {
            return getCurrentSession()
                    .byMultipleIds(Scope.class)
                    .enableSessionCheck(true) // Проверяем кэш для производительности
                    .multiLoad(scopeIds)
                    .stream().filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to scopes by ids", e);
        }
    }
}
