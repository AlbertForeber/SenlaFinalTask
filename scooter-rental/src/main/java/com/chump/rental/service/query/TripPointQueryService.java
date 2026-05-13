package com.chump.rental.service.query;

import com.chump.common.utils.TransactionUtils;
import com.chump.rental.dao.TripPointDao;
import com.chump.rental.dto.response.TripPointResponse;
import com.chump.rental.mapper.TripPointMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TripPointQueryService {

    private final TripPointDao tripPointDao;
    private final TripPointMapper tripPointMapper;
    private final TransactionUtils transactionUtils;

    public TripPointQueryService(TripPointDao tripPointDao, TripPointMapper tripPointMapper, TransactionUtils transactionUtils) {
        this.tripPointDao = tripPointDao;
        this.tripPointMapper = tripPointMapper;
        this.transactionUtils = transactionUtils;
    }

    @Transactional(readOnly = true)
    public List<TripPointResponse> getTripPoints(int tripId, int pageSize, int page) {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got all rental spots hierarchy")
        );

        return tripPointMapper.toResponseList(tripPointDao.batchFindByTripId(tripId, pageSize, page - 1));
    }
}
