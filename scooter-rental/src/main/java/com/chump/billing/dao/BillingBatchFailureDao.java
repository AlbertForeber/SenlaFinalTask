package com.chump.billing.dao;

import com.chump.billing.model.BillingBatchFailure;
import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillingBatchFailureDao extends AbstractHibernateDao<BillingBatchFailure, Integer> {

    public BillingBatchFailureDao(SessionFactory sessionFactory) {
        super(BillingBatchFailure.class, sessionFactory);
    }

    public List<BillingBatchFailure> batchFindAllWithItemsSortedByFailedAt(int batchSize, int offset) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<BillingBatchFailure> query = criteriaBuilder.createQuery(BillingBatchFailure.class);

            Root<BillingBatchFailure> root = query.from(BillingBatchFailure.class);
            query.orderBy(criteriaBuilder.desc(root.get("failedAt")), criteriaBuilder.asc(root.get("id")));

            return getCurrentSession()
                    .createQuery(query)
                    .setFirstResult(offset * batchSize)
                    .setMaxResults(batchSize)
                    .getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find billing failure with items", e);
        }
    }
}
