package com.chump.rental.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.rental.dto.entry.WaypointEntry;
import com.chump.rental.model.TripPoint;
import com.chump.rental.model.TripPointId;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TripPointDao extends AbstractHibernateDao<TripPoint, TripPointId> {

    private final int batchSize;

    public TripPointDao(
            SessionFactory sessionFactory,
            @Value("${hibernate.jdbc.batch-size}") int batchSize
    ) {
        super(TripPoint.class, sessionFactory);
        this.batchSize = batchSize;
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

    public void batchSave(int tripId, List<WaypointEntry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            WaypointEntry entry = entries.get(i);

            TripPoint tripPoint = TripPoint.builder()
                    .id(TripPointId.builder()
                            .tripId(tripId)
                            .createdAt(entry.getSendAt())
                            .build())
                    .location(entry.getLocation())
                    .build();

            getCurrentSession().persist(tripPoint);

            if (i % batchSize == 0) {
                getCurrentSession().flush();
                getCurrentSession().clear();
            }

            getCurrentSession().flush();
            getCurrentSession().clear();
        }
    }
}