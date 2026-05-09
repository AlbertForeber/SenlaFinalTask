package com.chump.rental.service.query;

import com.chump.rental.dao.TripPointDao;
import com.chump.rental.dto.response.TripPointResponse;
import com.chump.rental.mapper.TripPointMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TripPointQueryService {

    private final TripPointDao tripPointDao;
    private final TripPointMapper tripPointMapper;

    public TripPointQueryService(TripPointDao tripPointDao, TripPointMapper tripPointMapper) {
        this.tripPointDao = tripPointDao;
        this.tripPointMapper = tripPointMapper;
    }

    @Transactional(readOnly = true)
    public List<TripPointResponse> getTripPoints(int tripId, int pageSize, int page) {
        return tripPointMapper.toResponseList(tripPointDao.batchFindByTripId(tripId, pageSize, page - 1));
    }
}
