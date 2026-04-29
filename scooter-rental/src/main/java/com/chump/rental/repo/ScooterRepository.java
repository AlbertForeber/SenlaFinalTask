package com.chump.rental.repo;

import com.chump.common.dto.param.GeoSearchParams;
import com.chump.rental.dao.ScooterPostgresDao;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.status.ScooterStatus;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScooterRepository {

    private final ScooterPostgresDao postgresDao;

    public List<Scooter> findAll() {
        return postgresDao.findAll();
    }

    public Optional<Scooter> findById(int scooterId) {
        return postgresDao.findById(scooterId);
    }

    public Scooter save(Scooter entity) {
        return postgresDao.save(entity);
    }

    public void update(Scooter entity) {
        postgresDao.update(entity);
    }

    public void delete(int scooterId) {
        postgresDao.delete(scooterId);
    }

    public List<Scooter> findAllInZone(Polygon polygon) {
        return postgresDao.findAllInZone(polygon);
    }

    public List<Scooter> findAllNearby(GeoSearchParams params) {
        return postgresDao.findAllNearby(params);
    }

    public List<Scooter> findByStatus(ScooterStatus status) {
        return postgresDao.findByStatus(status);
    }

    public List<Scooter> findByIds(List<Integer> scooterIds) {
        return postgresDao.findByIds(scooterIds);
    }
 }
