package com.chump.user.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.user.model.Role;
import com.chump.user.model.User;
import jakarta.persistence.criteria.*;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserDao extends AbstractHibernateDao<User, Integer> {

    public UserDao(SessionFactory sessionFactory) {
        super(User.class, sessionFactory);
    }

    public Optional<User> findByUsernameWithScopes(String username) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);

            Root<User> root = query.from(User.class);
            Fetch<User, Role> roleJoin = root.fetch("role");
            roleJoin.fetch("scopes", JoinType.LEFT); // LEFT для обработки пустой роли

            query.where(criteriaBuilder.equal(root.get("username"), username));
            return getCurrentSession().createQuery(query).uniqueResultOptional();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find user with username: " + username, e);
        }
    }

    public boolean existsByUsername(String username) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
            Root<User> root = query.from(User.class);

            query.where(criteriaBuilder.equal(root.get("username"), 1));

            return !getCurrentSession()
                    .createQuery(query)
                    .setMaxResults(1)
                    .getResultList()
                    .isEmpty();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to check if users exists with username: " + username, e);
        }
    }
}
