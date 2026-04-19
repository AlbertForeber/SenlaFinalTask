package com.chump.billing.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.billing.model.SubscriptionTariff;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SubscriptionTariffDao extends AbstractHibernateDao<SubscriptionTariff, Integer> {

    public SubscriptionTariffDao(SessionFactory sessionFactory) {
        super(SubscriptionTariff.class, sessionFactory);
    }

    public Optional<SubscriptionTariff> findByIdWithTariff(int id) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<SubscriptionTariff> query = criteriaBuilder.createQuery(SubscriptionTariff.class);

            Root<SubscriptionTariff> root = query.from(SubscriptionTariff.class);
            root.fetch("tariff");

            query.where(criteriaBuilder.equal(root.get("id"), id));
            return getCurrentSession().createQuery(query).uniqueResultOptional();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find subscription tariff with id: " + id, e);
        }
    }
}
