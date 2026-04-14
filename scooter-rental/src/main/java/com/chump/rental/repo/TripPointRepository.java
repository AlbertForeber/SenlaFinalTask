package com.chump.rental.repo;

import com.chump.rental.dao.TripPointDao;
import com.chump.rental.model.TripPoint;
import com.chump.rental.model.TripPointId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TripPointRepository {

    private final TripPointDao postgresDao;

    public TripPointRepository(TripPointDao postgresDao) {
        this.postgresDao = postgresDao;
    }

    public Optional<TripPoint> findById(TripPointId tripPointId) {
        return postgresDao.findById(tripPointId);
    }

    public List<TripPoint> findAll() {
        return postgresDao.findAll();
    }

    public TripPoint save(TripPoint entity) {
        return postgresDao.save(entity);
    }

    public void update(TripPoint entity) {
        postgresDao.update(entity);
    }

    public void delete(TripPointId tripPointId) {
        postgresDao.delete(tripPointId);
    }

    public List<TripPoint> findByTripId(int tripId) {
        return postgresDao.findByTripId(tripId);
    }
}
