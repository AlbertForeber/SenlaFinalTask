package com.chump.rental.dao;

import com.chump.common.dao.AbstractHibernateDao;
import com.chump.common.dto.param.GeoSearchParams;
import com.chump.common.exception.DataManipulationException;
import com.chump.rental.model.RentalSpot;
import org.geolatte.geom.jts.JTS;
import org.hibernate.SessionFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RentalSpotDao extends AbstractHibernateDao<RentalSpot, Integer> {

    public RentalSpotDao(SessionFactory sessionFactory) {
        super(RentalSpot.class, sessionFactory);
    }

    public List<RentalSpot> findAllNearby(GeoSearchParams params) {
        try {
            String sql = """
                SELECT * FROM rent_spots
                WHERE ST_DWithin(
                    area,
                    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
                    :rad
                ) AND is_parking=true;
                """;

        return getCurrentSession()
                .createNativeQuery(sql, RentalSpot.class)
                .setParameter("lon", params.getLongitude())
                .setParameter("lat", params.getLatitude())
                .setParameter("rad", params.getRadius())
                .getResultList();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find near rental spots", e);
        }
    }

    public boolean isInParkingRentalSpot(Point point) {
        try {
            String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM rent_spots
                    WHERE ST_Within(
                        CAST(:point AS geometry),
                        CAST(area AS geometry)
                    ) AND is_parking = true
                )
                """;

            return getCurrentSession()
                    .createNativeQuery(sql, Boolean.class)
                    .setParameter("point", point)
                    .getSingleResult();
        } catch (Exception e) {
            throw new DataManipulationException("Failed to check if point in rental spot", e);
        }
    }

    public List<RentalSpot> findAllWithChildren() {
        try {
            String sql = """
                WITH RECURSIVE tree AS (
                    SELECT id, name, area, parent_id, is_parking, 0 AS depth
                    FROM rent_spots
                    WHERE parent_id IS NULL

                    UNION ALL

                    SELECT rs.id, rs.name, rs.area, rs.parent_id, rs.is_parking, tree.depth + 1 AS depth
                    FROM rent_spots rs
                    JOIN tree ON rs.parent_id = tree.id
                )
                SELECT * FROM tree;
                """;

            List<Object[]> rows = getCurrentSession()
                    .createNativeQuery(sql, Object[].class)
                    .getResultList();

            return buildTree(rows);
        } catch (Exception e) {
            throw new DataManipulationException("Failed to get rental spots hierarchy", e);
        }
    }

    public Optional<RentalSpot> findByIdWithParents(int rentSpotId) {
        try {
            // 1) Кастить с :: не можем, т.к. hibernate воспринимает как именованный параметр
            // 2) CAST не используем, данные приходят в виде JTS, которые разбирает hibernate spatial
            // 3) COALESCE простой способ определить корневой объект, уровень не важен
            //    важно знать что вернуть, а вернуть нужно корневой объект для корректного отображения
            String sql = """
                WITH RECURSIVE tree AS (
                    SELECT id, name, area, parent_id, is_parking, COALESCE(parent_id, 0) AS depth
                    FROM rent_spots
                    WHERE id = :id

                    UNION ALL

                    SELECT rs.id, rs.name, rs.area, rs.parent_id, rs.is_parking, COALESCE(rs.parent_id, 0) AS depth
                    FROM rent_spots rs
                    JOIN tree ON rs.id = tree.parent_id
                )
                SELECT * FROM tree;
                """;

            List<Object[]> rows = getCurrentSession()
                    .createNativeQuery(sql, Object[].class)
                    .setParameter("id", rentSpotId)
                    .getResultList();

            if (rows.isEmpty())
                return Optional.empty();

            return Optional.of(buildTree(rows).get(0));
        } catch (Exception e) {
            throw new DataManipulationException("Failed to find rental spot with parents with id: " + rentSpotId, e);
        }
    }

    private List<RentalSpot> buildTree(List<Object[]> rows) {
        Map<Integer, RentalSpot> tree = new HashMap<>();
        List<RentalSpot> roots = new ArrayList<>();

        for (Object[] o : rows) {
            RentalSpot spot = new RentalSpot(
                    ((Number) o[0]).intValue(),                                // id
                    null,
                    new ArrayList<>(),
                    (String) o[1],                                             // name
                    JTS.to((org.geolatte.geom.Polygon<?>) o[2]),               // area
                    (Boolean) o[4]
            );

            tree.put(spot.getId(), spot);

            // depth
            if (((Number) o[5]).intValue() == 0) {
                roots.add(spot);
            }
        }

        for (Object[] o : rows) {
            Integer id = ((Number) o[0]).intValue();
            Integer parentId = o[3] != null ? ((Number) o[3]).intValue() : null;

            if (parentId != null) {
                RentalSpot current = tree.get(id);
                RentalSpot parent = tree.get(parentId);

                // Метод нужен для проходу по дереву вниз (прародитель <-X- родитель -> дети -> дети детей)
                // Поэтому потеря информации о родителе конкретной точки нестрашна
                if (parent != null) {
                    current.setParent(parent);
                    parent.getChildren().add(current);
                }
            }
        }

        return roots;
    }
}
