package com.chump.user.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.user.model.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RoleDao extends AbstractHibernateDao<Role, Integer> {

    private final int defaultRoleId;

    public RoleDao(SessionFactory sessionFactory,
                   @Value("${role.default-role.id}") int defaultRoleId) {
        super(Role.class, sessionFactory);
        this.defaultRoleId = defaultRoleId;
    }

    public Optional<Role> getDefaultRole() {
        return findById(defaultRoleId);
    }

    public Optional<Role> findByIdWithScopes(int roleId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Role> query = criteriaBuilder.createQuery(Role.class);

            Root<Role> root = query.from(Role.class);
            root.fetch("scopes", JoinType.LEFT);

            query.where(criteriaBuilder.equal(root.get("id"), roleId));
            return getCurrentSession().createQuery(query).uniqueResultOptional();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find role with scopes", e);
        }
    }

    public void clearRoleScopes(int roleId) {
        String sql = """
                DELETE FROM role_scope
                WHERE role_id = :id
                """;

        getCurrentSession()
                .createNativeMutationQuery(sql)
                .setParameter("id", roleId)
                .executeUpdate();
    }

    public void batchInsertRoleScopes(int roleId, List<Integer> scopesIds) {
        String sql = """
                INSERT INTO role_scope
                SELECT :roleId, unnest(:scopeIds)
                """;

        Integer[] scopesIdsArray = scopesIds.toArray(new Integer[0]);
        getCurrentSession()
                .createNativeMutationQuery(sql)
                .setParameter("roleId", roleId)
                .setParameter("scopeIds", scopesIdsArray)
                .executeUpdate();
    }
}