package com.chump.rental.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.utils.TransactionUtils;
import com.chump.rental.dao.RentalSpotDao;
import com.chump.rental.dto.command.RentalSpotCommand;
import com.chump.rental.dto.response.RentalSpotDetailedResponse;
import com.chump.rental.mapper.RentalSpotMapper;
import com.chump.rental.model.RentalSpot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalSpotService {

    private final RentalSpotMapper mapper;
    private final RentalSpotDao dao;
    private final TransactionUtils transactionUtils;

    @Transactional
    public RentalSpotDetailedResponse openSpot(RentalSpotCommand command) {
        Integer parentId = command.getParentId();
        RentalSpot parent = parentId != null ? dao.findById(parentId).orElseThrow(
                () -> new NoSuchEntityException("No parent rental spot found with id: " + parentId)
        ) : null;

        RentalSpot spot = dao.save(mapper.toEntity(command, parent));

        transactionUtils.afterCommit(() ->
                log.info("Successfully opened spot with id: {}", spot.getId())
        );

        return mapper.toDetailedResponse(spot);
    }

    @Transactional
    public RentalSpotDetailedResponse updateSpotInfo(int rentSpotId, RentalSpotCommand command) {
        RentalSpot point = dao.findById(rentSpotId).orElseThrow(
                () -> new NoSuchEntityException("No rental point found to update with id: " + rentSpotId)
        );

        Integer parentId = command.getParentId();
        RentalSpot parent = parentId != null ? dao.findById(parentId).orElseThrow(
                () -> new NoSuchEntityException("No parent rental spot found with id: " + parentId)
        ) : null;

        mapper.updateRentalPointFromCommand(command, parent, point);

        // ...AutoUpdate через DirtyCheck

        transactionUtils.afterCommit(() ->
                log.info("Successfully updated spot with id: {}", rentSpotId)
        );

        return mapper.toDetailedResponse(point);
    }

    @Transactional
    public void closeSpot(int rentSpotId) {
        dao.delete(rentSpotId);
        log.info("Successfully closed spot with id: {}", rentSpotId);
    }
}
