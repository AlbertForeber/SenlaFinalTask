package com.chump.user.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.user.model.Role;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
}