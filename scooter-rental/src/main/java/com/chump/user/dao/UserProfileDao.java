package com.chump.user.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.user.model.UserProfile;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserProfileDao extends AbstractHibernateDao<UserProfile, Integer> {

    public UserProfileDao(SessionFactory sessionFactory) {
        super(UserProfile.class, sessionFactory);
    }

    public Optional<UserProfile> findByIdWithUser(int userId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<UserProfile> query = criteriaBuilder.createQuery(UserProfile.class);

            Root<UserProfile> root = query.from(UserProfile.class);
            root.fetch("user");
            query.where(criteriaBuilder.equal(root.get("id"), userId));

            return getCurrentSession().createQuery(query).uniqueResultOptional();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find entity by id: " + userId, e);
        }
    }
}
