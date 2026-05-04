package com.chump.rental.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.rental.dto.RouteData;
import com.chump.rental.model.Trip;
import com.chump.rental.model.status.TripStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.geolatte.geom.jts.JTS;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
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
            query.where(criteriaBuilder.equal(root.get("status"), TripStatus.ONGOING));

            return getCurrentSession().createQuery(query).uniqueResultOptional();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find ongoing trip for scooter with id: " + scooterId, e);
        }
    }

    public List<Trip> batchFindByUserId(int userId, int batchSize, int offset) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Trip> query = criteriaBuilder.createQuery(Trip.class);
            Root<Trip> root = query.from(Trip.class);

            query.where(criteriaBuilder.equal(root.get("user").get("id"), userId));
            return getCurrentSession()
                    .createQuery(query)
                    .setFirstResult(offset * batchSize)
                    .setMaxResults(batchSize)
                    .getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find trips for user with id: " + userId, e);
        }
    }

    public List<Trip> batchFindByScooterId(int scooterId, int batchSize, int offset) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Trip> query = criteriaBuilder.createQuery(Trip.class);
            Root<Trip> root = query.from(Trip.class);

            query.where(criteriaBuilder.equal(root.get("scooter").get("id"), scooterId));
            return getCurrentSession()
                    .createQuery(query)
                    .setFirstResult(offset * batchSize)
                    .setMaxResults(batchSize)
                    .getResultList();
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

    public RouteData updateRouteAndDistance(int tripId) {
        try {
            String sql = """
                    WITH line AS (
                        SELECT CASE
                            WHEN COUNT(*) < 2 THEN NULL
                            ELSE ST_SetSRID(ST_MakeLine(CAST(location AS geometry)), 4326)
                        END AS geom
                        FROM trip_points WHERE trip_id = :id
                    )
                    UPDATE trips SET
                        route = CAST(ST_SimplifyPreserveTopology(line.geom, 0.001) AS geography),
                        distance = COALESCE(ST_Length(CAST(line.geom AS geography)), 0)
                    FROM line
                    WHERE trips.id = :id
                    RETURNING route, distance
                    """;
            String oldSql = """
                    UPDATE trips SET
                         route = (
                            SELECT CASE
                                WHEN COUNT(*) < 2 THEN NULL
                                ELSE CAST(ST_SetSRID(ST_SimplifyPreserveTopology(ST_MakeLine(CAST(location AS geometry)), 0.001), 4326) AS geography)
                            END
                            FROM trip_points WHERE trip_id = :id
                        ),
                        distance = (
                            SELECT CASE
                                WHEN COUNT(*) < 2 THEN 0
                                ELSE ST_Length(CAST(ST_MakeLine(CAST(location AS geometry)) AS geography))
                            END
                        )
                    WHERE id = :id
                    RETURNING route, ST_Length(route)
                    """; // TODO убрать

            Object[] results = getCurrentSession()
                    .createNativeQuery(sql, Object[].class)
                    .setParameter("id", tripId)
                    .getSingleResult();

            return new RouteData(
                    results[0] != null ? JTS.to((org.geolatte.geom.LineString<?>) results[0]) : null,
                    (double) results[1]);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to update route for trip with id: " + tripId, e);
        }
    }
}
