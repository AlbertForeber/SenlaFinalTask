package com.chump.rental.controller;

import com.chump.common.dto.param.GeoSearchParams;
import com.chump.rental.dto.request.CreateScooterRequest;
import com.chump.rental.dto.request.UpdateScooterInfoRequest;
import com.chump.rental.dto.response.ScooterModelResponse;
import com.chump.rental.dto.response.ScooterResponse;
import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.model.status.ScooterStatus;
import com.chump.rental.service.ScooterService;
import com.chump.rental.service.query.ScooterQueryService;
import com.chump.rental.service.query.TripQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/scooters")
public class ScooterController {

    private final ScooterQueryService scooterQueryService;
    private final TripQueryService tripQueryService;
    private final ScooterService scooterService;
    private final ScooterMapper scooterMapper;

    public ScooterController(ScooterQueryService scooterQueryService, TripQueryService tripQueryService, ScooterService scooterService, ScooterMapper scooterMapper) {
        this.scooterQueryService = scooterQueryService;
        this.tripQueryService = tripQueryService;
        this.scooterService = scooterService;
        this.scooterMapper = scooterMapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_scooter:view')")
    public ResponseEntity<List<ScooterResponse>> getAllFreeScooters() {
        return ResponseEntity.ok(scooterQueryService.getAllFreeScooters());
    }

    @GetMapping(params = {"longitude", "latitude", "radius"})
    @PreAuthorize("hasAuthority('SCOPE_scooter:view')")
    public ResponseEntity<List<ScooterResponse>> getNearbyFreeScooters(
            @RequestParam float longitude,
            @RequestParam float latitude,
            @RequestParam float radius
    ) {
        return ResponseEntity.ok(scooterQueryService.getNearbyScooters(
                GeoSearchParams.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .radius(radius)
                        .build()
        ));
    }

    @GetMapping(params = "status")
    @PreAuthorize("hasAuthority('SCOPE_scooter:view_by_status')")
    public ResponseEntity<List<ScooterResponse>> getScooterWithStatus(
            @RequestParam ScooterStatus status
    ) {
        return ResponseEntity.ok(scooterQueryService.getScooterByStatus(status));
    }

    @GetMapping("/models")
    @PreAuthorize("hasAuthority('SCOPE_scooter:view_admin')")
    public ResponseEntity<List<ScooterModelResponse>> getScooterModels() {
        return ResponseEntity.ok(scooterQueryService.getScooterModels());
    }

    @GetMapping("/{id}/trips")
    @PreAuthorize("hasAuthority('SCOPE_scooter:view_admin')")
    public ResponseEntity<List<TripConciseResponse>> getScooterHistory(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(tripQueryService.getScooterTrips(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_scooter:view')")
    @PostAuthorize("hasAuthority('SCOPE_scooter:view_by_status') || returnObject.body.status == 'FREE'")
    public ResponseEntity<ScooterResponse> getScooterInfo(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(scooterQueryService.getScooterInfo(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_scooter:manage')")
    public ResponseEntity<ScooterResponse> postScooter(
            @Valid @RequestBody CreateScooterRequest request
    ) {
        ScooterResponse result = scooterService.addScooter(
                scooterMapper.toCreateCommand(request)
        );
        URI location = URI.create("/api/scooters/" + result.getId());

        return ResponseEntity
                .created(location)
                .body(result);
    }

    @PatchMapping("/{id}/info")
    @PreAuthorize("hasAuthority('SCOPE_scooter:manage')")
    public ResponseEntity<ScooterResponse> patchScooterInfo(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateScooterInfoRequest request
    ) {
        ScooterResponse result = scooterService.updateScooterInfo(
                id, scooterMapper.toUpdateCommand(request)
        );
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_scooter:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScooter(
            @PathVariable Integer id
    ) {
        scooterService.writeOffScooter(id);
    }
}
