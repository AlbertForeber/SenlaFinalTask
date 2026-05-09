package com.chump.billing.dao;

import com.chump.billing.model.BillingBatchFailure;
import com.chump.billing.model.BillingBatchFailureItem;
import com.chump.billing.model.BillingBatchFailureItemId;
import com.chump.common.dao.AbstractHibernateDao;
import jakarta.persistence.criteria.*;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillingBatchFailureItemDao extends AbstractHibernateDao<BillingBatchFailureItem, BillingBatchFailureItemId> {

    private final int batchSize;

    public BillingBatchFailureItemDao(
            SessionFactory sessionFactory,
            @Value("${hibernate.jdbc.batch-size:50}") int batchSize
    ) {
        super(BillingBatchFailureItem.class, sessionFactory);
        this.batchSize = batchSize;
    }

    public void batchSave(int failureId, List<Integer> userSubscriptionIds) {
        BillingBatchFailure failure = getCurrentSession().getReference(BillingBatchFailure.class, failureId);

        for (int i = 0; i < userSubscriptionIds.size(); i++) {
            BillingBatchFailureItem toPersist = new BillingBatchFailureItem(BillingBatchFailureItemId.builder()
                    .failureId(failureId)
                    .userSubscriptionId(userSubscriptionIds.get(i))
                    .build(), failure);

            getCurrentSession().persist(toPersist);

            if (i % batchSize == 0) {
                getCurrentSession().flush();
                getCurrentSession().clear();
            }
        }
    }

    public List<BillingBatchFailureItem> batchFindNotResolvedWithFailure(int batchSize) {
        CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<BillingBatchFailureItem> query = criteriaBuilder.createQuery(BillingBatchFailureItem.class);

        Root<BillingBatchFailureItem> root = query.from(BillingBatchFailureItem.class);
        root.fetch("failure");

        query.from(BillingBatchFailureItem.class);
        query.where(criteriaBuilder.isFalse(root.get("failure").get("isResolved")));

        return getCurrentSession()
                .createQuery(query)
                .setMaxResults(batchSize)
                .getResultList();
    }
}
