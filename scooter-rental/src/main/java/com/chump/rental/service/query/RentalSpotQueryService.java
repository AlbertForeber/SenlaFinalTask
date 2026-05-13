package com.chump.rental.service.query;

import com.chump.common.dto.param.GeoSearchParams;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.utils.TransactionUtils;
import com.chump.rental.dao.RentalSpotDao;
import com.chump.rental.dto.response.RentalSpotConciseResponse;
import com.chump.rental.dto.response.RentalSpotDetailedResponse;
import com.chump.rental.dto.response.RentalSpotHierarchyResponse;
import com.chump.rental.dto.response.RentalSpotWithScootersResponse;
import com.chump.rental.mapper.RentalSpotMapper;
import com.chump.rental.model.RentalSpot;
import com.chump.rental.model.Scooter;
import com.chump.rental.repo.ScooterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalSpotQueryService {

    private final RentalSpotDao dao;
    private final ScooterRepository scooterRepository;
    private final RentalSpotMapper mapper;
    private final TransactionUtils transactionUtils;

    @Transactional(readOnly = true)
    public List<RentalSpotHierarchyResponse> getAllRentalSpotsHierarchy() {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got all rental spots hierarchy")
        );

        return mapper.toHierarchyResponseList(dao.findAllWithChildren());
    }

    @Transactional(readOnly = true)
    public RentalSpotHierarchyResponse getRentalSpotHierarchyUp(int rentSpotId) {
        RentalSpot result =  dao.findByIdWithParents(rentSpotId).orElseThrow(
                () -> new NoSuchEntityException("No rental spot found with id: " + rentSpotId)
        );

        transactionUtils.afterCommit(() ->
                log.info("Successfully got rental spot hierarchy up for rental spot with id: {}", rentSpotId)
        );

        return mapper.toHierarchyResponse(result);
    }

    @Transactional(readOnly = true)
    public RentalSpotWithScootersResponse getRentalSpotScooters(int rentSpotId) {
        // Выбрано 2 запроса, вместо одного, чтобы различать случаи, когда указанной зоны нет
        // и когда в указанной зоне нет свободных самокатов
        RentalSpot spot = dao.findById(rentSpotId).orElseThrow(
                () -> new NoSuchEntityException("No rental spot found with id: " + rentSpotId)
        );
        List<Scooter> scootersInZone = scooterRepository.findAllInZone(spot.getArea());

        transactionUtils.afterCommit(() ->
                log.info("Successfully got scooters in rental spot with id: {}", rentSpotId)
        );

        return mapper.toWithScootersResponse(spot, scootersInZone);
    }

    @Transactional(readOnly = true)
    public RentalSpotDetailedResponse getRentalSpotsDetailedInfo(int rentSpotId) {
        RentalSpot result = dao.findById(rentSpotId).orElseThrow(
                () -> new NoSuchEntityException("No rental spot found with id: " + rentSpotId)
        );

        transactionUtils.afterCommit(() ->
                log.info("Successfully got detailed info for rental spot with id: {}", rentSpotId)
        );

        return mapper.toDetailedResponse(result);
    }

    @Transactional(readOnly = true)
    public List<RentalSpotConciseResponse> getNearbySpots(GeoSearchParams params) {
        List<RentalSpot> result = dao.findAllNearby(params);

        transactionUtils.afterCommit(() ->
                log.info("Successfully got nearby spots with params: {}", params)
        );

        return mapper.toConciseResponseList(result);
    }
}
