package com.chump.rental.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.dto.param.GeoSearchParams;
import com.chump.common.exception.DataManipulationException;
import com.chump.rental.dto.entry.TelemetryEntry;
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
public class ScooterPostgresDao extends AbstractHibernateDao<Scooter, Integer> {

    public ScooterPostgresDao(SessionFactory sessionFactory) {
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

    public List<Scooter> findByIds(List<Integer> ids) {
        try {
            return getCurrentSession()
                    .byMultipleIds(Scooter.class)
                    .enableSessionCheck(true)
                    .multiLoad(ids);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find scooters with ids: " + ids, e);
        }
    }

    public void batchUpdateTelemetry(List<TelemetryEntry> entries) {
        int[] scooterIds = new int[entries.size()];
        int[] battery = new int[entries.size()];
        double[] longitude = new double[entries.size()];
        double[] latitude = new double[entries.size()];

        for (int i = 0; i < entries.size(); i ++) {
            scooterIds[i] = entries.get(i).getScooterId();
            battery[i] = entries.get(i).getBattery();
            longitude[i] = entries.get(i).getLongitude();
            latitude[i] = entries.get(i).getLatitude();
        }

        String sql = """
                UPDATE scooters s SET
                    s.location = ST_SetSRID(ST_MakePoint(v.longitude, v.latitude), 4326)::geography,
                    s.battery = v.battery
                FROM unnest(:scooterIds, :longitude, :latitude, :battery) AS v(scooter_id, longitude, latitude, battery)
                WHERE s.id = v.scooter_id
                """;

        getCurrentSession()
                .createNativeMutationQuery(sql)
                .setParameter("scooterIds", scooterIds)
                .setParameter("longitude", longitude)
                .setParameter("latitude", latitude)
                .setParameter("battery", battery)
                .executeUpdate();
    }
}
