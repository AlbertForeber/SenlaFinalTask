package com.chump.rental.service.query;

import com.chump.common.dto.param.GeoSearchParams;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.rental.dao.ScooterModelDao;
import com.chump.rental.dto.response.ScooterModelResponse;
import com.chump.rental.dto.response.ScooterResponse;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.mapper.ScooterModelMapper;
import com.chump.rental.model.status.ScooterStatus;
import com.chump.rental.repo.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScooterQueryService {

    private final ScooterRepository repo;
    private final ScooterModelDao modelDao;
    private final ScooterMapper mapper;
    private final ScooterModelMapper modelMapper;

    @Transactional(readOnly = true)
    public ScooterResponse getScooterInfo(int scooterId) {
        return mapper.toResponse(repo.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        ));
    }

    @Transactional(readOnly = true)
    public List<ScooterResponse> getAllFreeScooters() {
        return mapper.toResponseList(repo.findByStatus(ScooterStatus.FREE));
    }

    @Transactional(readOnly = true)
    public List<ScooterResponse> getNearbyScooters(GeoSearchParams params) {
        return mapper.toResponseList(repo.findAllNearby(params));
    }

    @Transactional(readOnly = true)
    public List<ScooterResponse> getScooterByStatus(ScooterStatus status) {
        return mapper.toResponseList(repo.findByStatus(status));
    }

    @Transactional(readOnly = true)
    public List<ScooterModelResponse> getScooterModels() {
        return modelMapper.toResponseList(modelDao.findAll());
    }
}
