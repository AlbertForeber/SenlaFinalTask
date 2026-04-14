package com.chump.tariff.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.tariff.model.Tariff;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TariffDao extends AbstractHibernateDao<Tariff, Integer> {

    private final int defaultTariffId;

    public TariffDao(SessionFactory sessionFactory,
                     @Value("${tariff.default-tariff.id}") Integer defaultTariffId) {
        super(Tariff.class, sessionFactory);
        this.defaultTariffId = defaultTariffId;
    }

    public Optional<Tariff> getDefaultTariff() {
        return findById(defaultTariffId);
    }
}
