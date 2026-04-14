package com.chump.user.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.user.model.UserSubscription;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserSubscriptionDao extends AbstractHibernateDao<UserSubscription, Integer> {

    public UserSubscriptionDao(SessionFactory sessionFactory) {
        super(UserSubscription.class, sessionFactory);
    }

    public Optional<UserSubscription> findByUserIdWithTariff(int userId) {
        CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<UserSubscription> query = criteriaBuilder.createQuery(UserSubscription.class);

        Root<UserSubscription> root = query.from(UserSubscription.class);
        query.where(criteriaBuilder.equal(root.get("user").get("id"), userId));

        root.fetch("tariff");
        return getCurrentSession().createQuery(query).uniqueResultOptional();
    }
}