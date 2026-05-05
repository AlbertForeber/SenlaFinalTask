package com.chump.auth.dao;

import com.chump.auth.model.Session;
import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class SessionDao extends AbstractHibernateDao<Session, Integer> {

    private final long refreshTokenRetentionTime;

    public SessionDao(
            SessionFactory sessionFactory,
            @Value("${auth.refresh-token.retention-time}") long refreshTokenRetentionTime
    ) {
        super(Session.class, sessionFactory);
        this.refreshTokenRetentionTime = refreshTokenRetentionTime;
    }

    public Optional<Session> findByToken(String token) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Session> query = criteriaBuilder.createQuery(Session.class);
            Root<Session> root = query.from(Session.class);

            query.where(criteriaBuilder.equal(root.get("refreshToken"), token));
            root.fetch("user"); // Для помощи в генерации accessToken при ротации

            return getCurrentSession().createQuery(query).uniqueResultOptional();
        } catch (Exception e) {
            throw new DataManipulationException(
                    "Failed to find refresh token by token: " + token.substring(0, 7) + "...",
                    e
            );
        }
    }

    public void terminateChain(String token) {
        try {
            String sql = """
                    WITH RECURSIVE token_chain AS (
                        SELECT refresh_token, replaced_by_token
                        FROM sessions
                        WHERE refresh_token = :token

                        UNION ALL

                        SELECT s.refresh_token, s.replaced_by_token
                        FROM sessions s
                        JOIN token_chain ON s.refresh_token = token_chain.replaced_by_token
                    )
                    UPDATE sessions SET
                        terminated = true
                    FROM token_chain
                    WHERE sessions.refresh_token = token_chain.refresh_token
                    """;
            getCurrentSession()
                    .createNativeMutationQuery(sql)
                    .setParameter("token", token)
                    .executeUpdate();
        } catch (Exception e) {
            throw new DataManipulationException(
                    "Failed to revoke refresh tokens chain for token: " + token.substring(0, 7) + "...",
                    e
            );
        }
    }

    public int deleteStale() {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaDelete<Session> delete = criteriaBuilder.createCriteriaDelete(Session.class);

            Root<Session> root = delete.from(Session.class);
            delete.where(criteriaBuilder.and(
                    criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), Instant.now().minusMillis(refreshTokenRetentionTime)),
                    criteriaBuilder.isTrue(root.get("terminated"))
            ));

            return getCurrentSession().createMutationQuery(delete).executeUpdate();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to delete stall refresh tokens", e);
        }
    }

    public void terminateByUserId(int userId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaUpdate<Session> update = criteriaBuilder.createCriteriaUpdate(Session.class);

            Root<Session> root = update.from(Session.class);
            update.where(criteriaBuilder.equal(root.get("user").get("id"), userId));
            update.set(root.get("terminated"), true);

            getCurrentSession().createMutationQuery(update).executeUpdate();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to revoke refresh tokens by user id", e);
        }
    }

    public List<Session> findAllActiveByUserId(int userId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Session> query = criteriaBuilder.createQuery(Session.class);

            Root<Session> root = query.from(Session.class);
            query.where(criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("user").get("id"), userId),
                    criteriaBuilder.isFalse(root.get("terminated"))
            ));

            return getCurrentSession().createQuery(query).getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find refresh tokens by user id: " + userId, e);
        }
    }
}
