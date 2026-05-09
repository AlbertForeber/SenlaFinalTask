package com.chump.user.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.user.model.UserSubscription;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

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
            query.where(criteriaBuilder.equal(root.get("userProfile").get("id"), userId));

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

    public List<Integer> batchFindToBillIds(int batchSize, int offset) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Integer> query = criteriaBuilder.createQuery(Integer.class);

            Root<UserSubscription> root = query.from(UserSubscription.class);

            query.where(criteriaBuilder.lessThanOrEqualTo(root.get("nextBillingDate"), LocalDate.now()));
            query.select(root.get("id"));
            query.orderBy(criteriaBuilder.asc(root.get("id")));

            return getCurrentSession()
                    .createQuery(query)
                    .setFirstResult(offset * batchSize)
                    .setMaxResults(batchSize)
                    .getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find subscription ids to bill", e);
        }
    }

    public List<String> batchDeleteUnableToPayReturnMails(List<Integer> userSubscriptionIds) {
        try {
            String sql = """
                    DELETE FROM user_subscriptions us
                    WHERE us.user_id IN (:ids)
                        AND (SELECT ud.balance FROM user_details ud WHERE ud.user_id = us.user_id)
                            < (SELECT t.base_price FROM tariffs t WHERE t.id = us.tariff_id)
                    RETURNING (SELECT ud.email FROM user_details ud WHERE ud.user_id IN (:ids))
                    """;

            return getCurrentSession()
                    .createNativeQuery(sql, String.class)
                    .setParameter("ids", userSubscriptionIds)
                    .getResultList();
        } catch (Exception e) {
            throw new DataManipulationException(
                    "Failed to delete unable to pay users with ids: " + userSubscriptionIds, e
            );
        }
    }

    public void batchProcessBilling(List<Integer> userSubscriptionIds) {
        try {
            String sql = """
                    UPDATE user_details SET balance = balance -
                        COALESCE((SELECT t.base_price FROM tariffs t
                                WHERE t.id = (SELECT us.tariff_id FROM user_subscriptions us
                                                          WHERE us.user_id = user_details.user_id)), 0)
                    WHERE user_details.user_id IN (:ids)
                    """;

            getCurrentSession()
                    .createNativeMutationQuery(sql)
                    .setParameter("ids", userSubscriptionIds)
                    .executeUpdate();
        } catch (Exception e) {
            throw new DataManipulationException(
                    "Failed to process billing for users with ids:" + userSubscriptionIds, e
            );
        }
    }

    public void batchUpdateLastBillingDate(List<Integer> userSubscriptionsIds) {
        try {
            String sql = """
                    UPDATE user_subscriptions SET next_billing_date = next_billing_date +
                        (SELECT st.duration_days FROM subscription_tariffs st
                                                 WHERE st.tariff_id = user_subscriptions.tariff_id)
                    WHERE user_id IN (:ids)
                    """;

            getCurrentSession()
                    .createNativeMutationQuery(sql)
                    .setParameter("ids", userSubscriptionsIds)
                    .executeUpdate();
        } catch (Exception e) {
            throw new DataManipulationException(
                    "Failed to process update last billing date for users with ids:" + userSubscriptionsIds, e
            );
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