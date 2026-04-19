package com.chump.rental.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.dto.param.GeoSearchParams;
import com.chump.common.exception.DataManipulationException;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.status.ScooterStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.SessionFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScooterDao extends AbstractHibernateDao<Scooter, Integer> {

    public ScooterDao(SessionFactory sessionFactory) {
        super(Scooter.class, sessionFactory);
    }

    public List<Scooter> findAllInZone(Polygon polygon) {
        try {
            // Модель подтяигвается сразу в соответствии с требованием
            String sql = """
               SELECT
                   s.id AS s_id,
                   s.serial_no AS s_serial_no,
                   s.model_id AS s_model_id,
                   s.battery AS s_battery,
                   s.location AS s_location,
                   s.status AS s_status,
                   m.id AS m_id,
                   m.vendor AS m_vendor,
                   m.name As m_name
               FROM scooters s
               JOIN models m ON s.model_id = m.id
               WHERE ST_Within(
                    CAST(s.location AS geometry),
                    CAST(:polygon AS geometry)) AND s.status='FREE';
               """;

            return getCurrentSession()
                    .createNativeQuery(sql, "ScooterWithModelMapping", Object[].class)
                    .setParameter("polygon", polygon)
                    .getResultList()
                    .stream()
                    .map(o -> (Scooter) o[0])
                    .toList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find scooters in zone", e);
        }
    }

    public List<Scooter> findAllNearby(GeoSearchParams params) {
        try {
            String sql = """
                    SELECT * FROM scooters
               WHERE ST_DWithin(
               location,
               ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
               :rad
               ) AND status='FREE';
               """;


            return getCurrentSession().createNativeQuery(sql, Scooter.class)
                    .setParameter("lon", params.getLongitude())
                    .setParameter("lat", params.getLatitude())
                    .setParameter("rad", params.getRadius())
                    .getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find all nearby scooters", e);
        }
    }

    public List<Scooter> findByStatus(ScooterStatus status) {
        try {
            CriteriaBuilder criteriaBuilder = getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<Scooter> query = criteriaBuilder.createQuery(Scooter.class);
            Root<Scooter> root = query.from(Scooter.class);

            query.where(criteriaBuilder.equal(root.get("status"), status));
            return getCurrentSession().createQuery(query).getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find scooters with status: " + status, e);
        }
    }
}
