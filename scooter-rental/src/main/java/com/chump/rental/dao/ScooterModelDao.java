package com.chump.rental.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.rental.model.ScooterModel;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

@Component
public class ScooterModelDao extends AbstractHibernateDao<ScooterModel, Integer> {

    public ScooterModelDao(SessionFactory sessionFactory) {
        super(ScooterModel.class, sessionFactory);
    }
}
