package com.chump.rental.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.rental.model.Trip;
import com.chump.rental.model.status.TripStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class TripDao extends AbstractHibernateDao<Trip, Integer> {

    public TripDao(SessionFactory sessionFactory) {
        super(Trip.class, sessionFactory);
    }

    public Optional<Trip> findOngoingByScooterId(int scooterId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Trip> query = criteriaBuilder.createQuery(Trip.class);
            Root<Trip> root = query.from(Trip.class);

            query.where(criteriaBuilder.equal(root.get("scooter").get("id"), scooterId));
            return getCurrentSession().createQuery(query).uniqueResultOptional();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find ongoing trip for scooter with id: " + scooterId, e);
        }
    }

    public List<Trip> findByUserId(int userId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Trip> query = criteriaBuilder.createQuery(Trip.class);
            Root<Trip> root = query.from(Trip.class);

            query.where(criteriaBuilder.equal(root.get("user").get("id"), userId));
            return getCurrentSession().createQuery(query).getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find trips for user with id: " + userId, e);
        }
    }

    public List<Trip> findByScooterId(int scooterId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Trip> query = criteriaBuilder.createQuery(Trip.class);
            Root<Trip> root = query.from(Trip.class);

            query.where(criteriaBuilder.equal(root.get("scooter").get("id"), scooterId));
            return getCurrentSession().createQuery(query).getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find trips for scooter with id: " + scooterId, e);
        }
    }

    public List<Trip> findOngoingByUserId(int userId) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Trip> query = criteriaBuilder.createQuery(Trip.class);
            Root<Trip> root = query.from(Trip.class);

            query.where(criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("user").get("id"), userId)),
                    criteriaBuilder.equal(root.get("status"), TripStatus.ONGOING)
            );
            return getCurrentSession().createQuery(query).getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find trips for user with id: " + userId, e);
        }
    }

    public List<Trip> findFromPeriod(Instant from, Instant to) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Trip> query = criteriaBuilder.createQuery(Trip.class);
            Root<Trip> root = query.from(Trip.class);

            query.where(criteriaBuilder.between(root.get("startedAt"), from, to));
            return getCurrentSession().createQuery(query).getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find trips in period", e);
        }
    }
}
