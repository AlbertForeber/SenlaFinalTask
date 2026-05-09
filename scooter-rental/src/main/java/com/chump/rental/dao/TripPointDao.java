package com.chump.rental.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.exception.DataManipulationException;
import com.chump.rental.dto.entry.WaypointEntry;
import com.chump.rental.model.TripPoint;
import com.chump.rental.model.TripPointId;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
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

    public List<TripPoint> batchFindByTripId(int tripId, int batchSize, int offset) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<TripPoint> query = criteriaBuilder.createQuery(TripPoint.class);
            Root<TripPoint> root = query.from(TripPoint.class);

            query.where(criteriaBuilder.equal(root.get("id").get("tripId"), tripId));
            query.orderBy(criteriaBuilder.desc(root.get("id").get("createdAt")));

            return getCurrentSession()
                    .createQuery(query)
                    .setFirstResult(offset * batchSize)
                    .setMaxResults(batchSize)
                    .getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find trip points for trip with id: " + tripId, e);
        }
    }

    public void batchSave(int tripId, List<WaypointEntry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            WaypointEntry entry = entries.get(i);

            log.info("Processing entry {}", i); // TODO убрать

            TripPoint tripPoint = TripPoint.builder()
                    .id(TripPointId.builder()
                            .tripId(tripId)
                            .createdAt(entry.getSendAt())
                            .build())
                    .location(entry.getLocation())
                    .build();

            getCurrentSession().persist(tripPoint);

            if (i > 0 && i % batchSize == 0) {
                log.info("Flushed!"); // TODO убрать
                getCurrentSession().flush();
                getCurrentSession().clear();
            }
        }

        // TODO т.к. результат используется раньше конца транзакции
        // требуется дополнительный .flush
        log.info("Flushed final!"); // TODO убрать
        getCurrentSession().flush();
        getCurrentSession().clear();
    }
}