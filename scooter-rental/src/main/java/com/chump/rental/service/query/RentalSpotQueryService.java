package com.chump.rental.service.query;

import com.chump.common.dto.param.GeoSearchParams;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.rental.dao.RentalSpotDao;
import com.chump.rental.dto.response.RentalSpotConciseResponse;
import com.chump.rental.dto.response.RentalSpotHierarchyResponse;
import com.chump.rental.dto.response.RentalSpotWithScootersResponse;
import com.chump.rental.mapper.RentalSpotMapper;
import com.chump.rental.model.RentalSpot;
import com.chump.rental.model.Scooter;
import com.chump.rental.repo.ScooterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RentalSpotQueryService {

    private final RentalSpotDao dao;
    private final ScooterRepository scooterRepository;
    private final RentalSpotMapper mapper;

    public RentalSpotQueryService(RentalSpotDao dao, ScooterRepository scooterRepository, RentalSpotMapper mapper) {
        this.dao = dao;
        this.scooterRepository = scooterRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public RentalSpotHierarchyResponse getRentalSpotHierarchy(int rentSpotId) {
        RentalSpot result =  dao.findByIdWithChildren(rentSpotId).orElseThrow(
                () -> new NoSuchEntityException("No rental spot found with id: " + rentSpotId)
        );

        return mapper.toHierarchyResponse(result);
    }

    @Transactional(readOnly = true)
    public RentalSpotWithScootersResponse getRentalSpotInfo(int rentSpotId) {
        RentalSpot spot = dao.findById(rentSpotId).orElseThrow(
                () -> new NoSuchEntityException("No rental spot found with id: " + rentSpotId)
        );
        List<Scooter> scootersInZone = scooterRepository.findAllInZone(spot.getArea());

        return mapper.toWithScootersResponse(spot, scootersInZone);
    }

    @Transactional(readOnly = true)
    public List<RentalSpotConciseResponse> getNearbySpots(GeoSearchParams params) {
        List<RentalSpot> result = dao.findAllNearby(params);
        return mapper.toConciseResponseList(result);
    }
}
