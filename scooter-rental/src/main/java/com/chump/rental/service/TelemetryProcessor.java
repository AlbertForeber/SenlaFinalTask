package com.chump.rental.service;

import com.chump.rental.dto.entry.TelemetryEntry;
import com.chump.rental.model.Scooter;
import com.chump.rental.repo.ScooterRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TelemetryProcessor {

    private final static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final ScooterRepository scooterRepository;

    public TelemetryProcessor(ScooterRepository scooterRepository) {
        this.scooterRepository = scooterRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleBatch(List<TelemetryEntry> entries) {

    }
}
