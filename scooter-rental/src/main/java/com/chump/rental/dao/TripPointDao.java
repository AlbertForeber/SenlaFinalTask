package com.chump.rental.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.rental.model.TripPoint;
import com.chump.rental.model.TripPointId;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TripPointDao extends AbstractHibernateDao<TripPoint, TripPointId> {

    public TripPointDao(SessionFactory sessionFactory) {
        super(TripPoint.class, sessionFactory);
    }

    public List<TripPoint> findByTripId(int tripId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<TripPoint> query = criteriaBuilder.createQuery(TripPoint.class);
            Root<TripPoint> root = query.from(TripPoint.class);

            query.where(criteriaBuilder.equal(root.get("id").get("tripId"), tripId));
            return getCurrentSession().createQuery(query).getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find trip points for trip with id: " + tripId, e);
        }
    }
}
