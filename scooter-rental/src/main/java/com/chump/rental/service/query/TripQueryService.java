package com.chump.rental.service.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.rental.dao.TripDao;
import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.dto.response.TripDetailedResponse;
import com.chump.rental.mapper.TripMapper;
import com.chump.rental.model.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripQueryService {

    private final TripDao dao;
    private final TripMapper mapper;

    @Transactional(readOnly = true)
    public List<TripConciseResponse> getUserTrips(int userId, int pageSize, int page) {
        return mapper.toConsiseResponseList(dao.batchFindByUserId(userId, pageSize, page - 1));
    }

    @Transactional(readOnly = true)
    public List<TripConciseResponse> getScooterTrips(int scooterId, int pageSize, int page) {
        return mapper.toConsiseResponseList(dao.batchFindByScooterId(scooterId, pageSize, page - 1));
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

        return mapper.toDetailedResponse(trip);
    }
}