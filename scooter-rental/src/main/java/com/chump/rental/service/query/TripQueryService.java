package com.chump.rental.service.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.rental.dao.TripDao;
import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.dto.response.TripDetailedResponse;
import com.chump.rental.mapper.TripMapper;
import com.chump.rental.model.Trip;
import com.chump.rental.model.TripPoint;
import com.chump.rental.repo.TripPointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TripQueryService {

    private final TripDao dao;
    private final TripPointRepository pointRepository;
    private final TripMapper mapper;

    public TripQueryService(TripDao dao, TripPointRepository pointRepository, TripMapper mapper) {
        this.dao = dao;
        this.pointRepository = pointRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<TripConciseResponse> getUserTrips(int userId) {
        return mapper.toConsiseResponseList(dao.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<TripConciseResponse> getScooterTrips(int scooterId) {
        return mapper.toConsiseResponseList(dao.findByScooterId(scooterId));
    }

    @Transactional(readOnly = true)
    public List<TripConciseResponse> getTripsFromPeriod(Instant from, Instant to) {
        return mapper.toConsiseResponseList(dao.findFromPeriod(from, to));
    }

    @Transactional(readOnly = true)
    public List<TripConciseResponse> getOngoingTrips(int userId) {
        return mapper.toConsiseResponseList(dao.findOngoingByUserId(userId));
    }

    @Transactional(readOnly = true)
    public TripDetailedResponse getTripInfo(int tripId) {
        Trip trip = dao.findById(tripId).orElseThrow(
                () -> new NoSuchEntityException("No trip found with id: " + tripId)
        );

        List<TripPoint> tripPoints = pointRepository.findByTripId(trip.getId());
        return mapper.toDetailedResponse(trip, tripPoints);
    }
}