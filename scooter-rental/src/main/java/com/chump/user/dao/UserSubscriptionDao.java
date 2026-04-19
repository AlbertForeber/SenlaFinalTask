package com.chump.user.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.user.model.UserSubscription;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class UserSubscriptionDao extends AbstractHibernateDao<UserSubscription, Integer> {

    public UserSubscriptionDao(SessionFactory sessionFactory) {
        super(UserSubscription.class, sessionFactory);
    }

    public Optional<UserSubscription> findByIdWithTariff(int userId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<UserSubscription> query = criteriaBuilder.createQuery(UserSubscription.class);

            Root<UserSubscription> root = query.from(UserSubscription.class);
            query.where(criteriaBuilder.equal(root.get("user").get("id"), userId));

            root.fetch("tariff");
            return getCurrentSession().createQuery(query).uniqueResultOptional();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find user subscription for user with id: " + userId, e);
        }
    }

    public List<UserSubscription> findToBillWithUserProfile(LocalDate date, int batchSize) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<UserSubscription> query = criteriaBuilder.createQuery(UserSubscription.class);

            Root<UserSubscription> root = query.from(UserSubscription.class);
            root.fetch("userProfile");

            query.where(criteriaBuilder.lessThanOrEqualTo(root.get("nextBillingDate"), date));

            return getCurrentSession()
                    .createQuery(query)
                    .setMaxResults(batchSize)
                    .getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find subscriptions to bill", e);
        }
    }

    public List<UserSubscription> findByTariffIdWithUserProfile(int tariffId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<UserSubscription> query = criteriaBuilder.createQuery(UserSubscription.class);

            Root<UserSubscription> root = query.from(UserSubscription.class);
            root.fetch("userProfile");

            query.where(criteriaBuilder.equal(root.get("tariff").get("id"), tariffId));

            return getCurrentSession()
                    .createQuery(query)
                    .getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find subscriptions by tariff id: " + tariffId, e);
        }
    }
}