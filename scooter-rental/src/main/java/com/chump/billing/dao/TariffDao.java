package com.chump.billing.dao;

import com.chump.billing.model.SubscriptionTariff;
import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.billing.dto.BillingData;
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
                     @Value("${tariff.default-tariff.id}") Integer defaultTariffId) {
        super(Tariff.class, sessionFactory);
        this.defaultTariffId = defaultTariffId;
    }

    public Optional<Tariff> getDefaultTariff() {
        return findById(defaultTariffId);
    }

    // TODO удалить если не нужен
    public Optional<BillingData> getBillingData(int tariffId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<BillingData> query = criteriaBuilder.createQuery(BillingData.class);

            Root<SubscriptionTariff> root = query.from(SubscriptionTariff.class);
            root.join("tariff");

            query.where(criteriaBuilder.equal(root.get("tariff").get("id"), tariffId));
            query.select(criteriaBuilder.construct(
                    BillingData.class,
                    root.get("tariff").get("basePrice"),
                    root.get("durationDays")
            ));

            return getCurrentSession()
                    .createQuery(query)
                    .uniqueResultOptional();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find billing data for tariff with id: " + tariffId, e);
        }
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
