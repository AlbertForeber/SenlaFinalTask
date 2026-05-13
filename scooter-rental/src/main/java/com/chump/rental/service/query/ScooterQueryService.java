package com.chump.rental.service.query;

import com.chump.common.dto.param.GeoSearchParams;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.utils.TransactionUtils;
import com.chump.rental.dao.ScooterModelDao;
import com.chump.rental.dto.response.ScooterModelResponse;
import com.chump.rental.dto.response.ScooterResponse;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.mapper.ScooterModelMapper;
import com.chump.rental.model.status.ScooterStatus;
import com.chump.rental.repo.ScooterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScooterQueryService {

    private final ScooterRepository repo;
    private final ScooterModelDao modelDao;
    private final ScooterMapper mapper;
    private final ScooterModelMapper modelMapper;
    private final TransactionUtils transactionUtils;

    @Transactional(readOnly = true)
    public ScooterResponse getScooterInfo(int scooterId) {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got info for scooter with id: {}", scooterId)
        );

        return mapper.toResponse(repo.findByIdWithActualInfoAndModel(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        ));
    }

    @Transactional(readOnly = true)
    public List<ScooterResponse> getAllFreeScooters(int pageSize, int page) {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got all free scooters")
        );

        return mapper.toResponseList(repo.batchFindByStatus(ScooterStatus.FREE, pageSize, page - 1));
    }

    @Transactional(readOnly = true)
    public List<ScooterResponse> getNearbyScooters(GeoSearchParams params) {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got nearby scooters with params: {}", params)
        );

        return mapper.toResponseList(repo.findAllNearby(params));
    }

    @Transactional(readOnly = true)
    public List<ScooterResponse> getScooterByStatus(ScooterStatus status, int pageSize, int page) {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got scooters by status: {}", status)
        );

        return mapper.toResponseList(repo.batchFindByStatus(status, pageSize, page - 1));
    }

    @Transactional(readOnly = true)
    public List<ScooterModelResponse> getScooterModels() {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got all scooter models")
        );

        return modelMapper.toResponseList(modelDao.findAll());
    }
}
