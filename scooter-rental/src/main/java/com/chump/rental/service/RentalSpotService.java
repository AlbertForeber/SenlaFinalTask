package com.chump.rental.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.rental.dao.RentalSpotDao;
import com.chump.rental.dto.command.RentalSpotCommand;
import com.chump.rental.dto.response.RentalSpotDetailedResponse;
import com.chump.rental.mapper.RentalSpotMapper;
import com.chump.rental.model.RentalSpot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalSpotService {

    private final RentalSpotMapper mapper;
    private final RentalSpotDao dao;

    @Transactional
    public RentalSpotDetailedResponse openPoint(RentalSpotCommand command) {
        Integer parentId = command.getParentId();
        RentalSpot parent = parentId != null ? dao.findById(parentId).orElseThrow(
                () -> new NoSuchEntityException("No parent rental spot found with id: " + parentId)
        ) : null;

        RentalSpot spot = dao.save(mapper.toEntity(command, parent));
        return mapper.toDetailedResponse(spot);
    }

    @Transactional
    public RentalSpotDetailedResponse updatePointInfo(int rentSpotId, RentalSpotCommand command) {
        RentalSpot point = dao.findById(rentSpotId).orElseThrow(
                () -> new NoSuchEntityException("No rental point found to update with id: " + rentSpotId)
        );

        Integer parentId = command.getParentId();
        RentalSpot parent = parentId != null ? dao.findById(parentId).orElseThrow(
                () -> new NoSuchEntityException("No parent rental spot found with id: " + parentId)
        ) : null;

        mapper.updateRentalPointFromCommand(command, parent, point);

        // ...AutoUpdate через DirtyCheck

        return mapper.toDetailedResponse(point);
    }

    @Transactional
    public void closeSpot(int rentSpotId) {
        dao.delete(rentSpotId);
    }
}
