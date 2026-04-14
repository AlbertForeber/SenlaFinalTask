package com.chump.common.dao;

import com.chump.common.exception.DataManipulationException;
import com.chump.common.exception.NoSuchEntityException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class AbstractHibernateDao<T, ID> implements GenericDao<T, ID> {

    private final Class<T> type;
    private final SessionFactory sessionFactory;

    public AbstractHibernateDao(Class<T> type, SessionFactory sessionFactory) {
        this.type = type;
        this.sessionFactory = sessionFactory;
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Optional<T> findById(ID id) {
        try {
            return Optional.ofNullable(getCurrentSession().get(type, id));
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find entity by id: " + id, e);
        }
    }

    @Override
    public List<T> findAll() {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
            query.select(query.from(type));

            return getCurrentSession().createQuery(query).getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find all entities", e);
        }
    }

    @Override
    public T save(T entity) {
        try {
            return getCurrentSession().merge(entity);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to save entity", e);
        }
    }

    @Override
    public void update(T entity) {
        try {
            getCurrentSession().merge(entity);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to update entity", e);
        }
    }

    @Override
    public void delete(ID id) {
        try {
            T entity = getCurrentSession().get(type, id);
            if (entity == null) {
                throw new NoSuchEntityException("No entity found with id: " + id);
            }

            getCurrentSession().remove(entity);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to remove entity with id: " + id, e);
        }
    }

    @Override
    public T getReference(ID id) {
        try {
            return getCurrentSession().getReference(type, id);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to get reference for entity with id: " + id, e);
        }
    }
}
