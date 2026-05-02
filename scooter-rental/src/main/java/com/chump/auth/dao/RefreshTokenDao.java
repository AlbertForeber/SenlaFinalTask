package com.chump.auth.dao;

import com.chump.auth.model.RefreshToken;
import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class RefreshTokenDao extends AbstractHibernateDao<RefreshToken, Integer> {

    public RefreshTokenDao(SessionFactory sessionFactory) {
        super(RefreshToken.class, sessionFactory);
    }

    public Optional<RefreshToken> findByToken(String token) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<RefreshToken> query = criteriaBuilder.createQuery(RefreshToken.class);
            Root<RefreshToken> root = query.from(RefreshToken.class);

            query.where(criteriaBuilder.equal(root.get("token"), token));
            root.fetch("user"); // Для помощи в генерации accessToken при ротации

            return getCurrentSession().createQuery(query).uniqueResultOptional();
        } catch (Exception e) {
            throw new DataManipulationException(
                    "Failed to find refresh token by token: " + token.substring(0, 7) + "...",
                    e
            );
        }
    }

    public void deleteByUserId(Integer userId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaDelete<RefreshToken> delete = criteriaBuilder.createCriteriaDelete(RefreshToken.class);
            Root<RefreshToken> root = delete.from(RefreshToken.class);

            delete.where(criteriaBuilder.equal(root.get("user").get("id"), userId));
            getCurrentSession().createMutationQuery(delete).executeUpdate();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to delete refresh tokens by user id: " + userId, e);
        }
    }

    public void revokeByUserId(Integer userId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaUpdate<RefreshToken> update = criteriaBuilder.createCriteriaUpdate(RefreshToken.class);
            Root<RefreshToken> root = update.from(RefreshToken.class);

            update.set(root.get("revoked"), true);
            update.where(criteriaBuilder.equal(root.get("user").get("id"), userId));

            getCurrentSession().createMutationQuery(update).executeUpdate();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to revoke refresh tokens by user id: " + userId, e);
        }
    }
}
