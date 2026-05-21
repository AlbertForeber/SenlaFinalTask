package com.chump.billing.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.billing.model.Tariff;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TariffDao extends AbstractHibernateDao<Tariff, Integer> {

    private final int defaultTariffId;

    public TariffDao(SessionFactory sessionFactory,
                     @Value("${tariff.default-tariff.id:0}") int defaultTariffId) {
        super(Tariff.class, sessionFactory);
        this.defaultTariffId = defaultTariffId;
    }

    public Optional<Tariff> findDefaultTariff() {
        return findById(defaultTariffId);
    }

    public List<Tariff> batchFindAllSubscriptionTariffs(int batchSize, int offset) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Tariff> query = criteriaBuilder.createQuery(Tariff.class);
            Root<Tariff> root = query.from(Tariff.class);

            query.where(criteriaBuilder.isNull(root.get("billingIntervalMinutes")));
            return getCurrentSession()
                    .createQuery(query)
                    .setFirstResult(offset * batchSize)
                    .setMaxResults(batchSize)
                    .getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find subscription tariffs", e);
        }
    }
}
