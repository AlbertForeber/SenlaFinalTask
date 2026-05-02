package com.chump.rental.controller;

import com.chump.common.dto.param.GeoSearchParams;
import com.chump.rental.dto.request.CreateRentalSpotRequest;
import com.chump.rental.dto.request.UpdateRentalSpotRequest;
import com.chump.rental.dto.response.RentalSpotConciseResponse;
import com.chump.rental.dto.response.RentalSpotDetailedResponse;
import com.chump.rental.dto.response.RentalSpotHierarchyResponse;
import com.chump.rental.dto.response.RentalSpotWithScootersResponse;
import com.chump.rental.mapper.RentalSpotMapper;
import com.chump.rental.service.RentalSpotService;
import com.chump.rental.service.query.RentalSpotQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rental-spots")
@RequiredArgsConstructor
public class RentalSpotController {

    private final RentalSpotQueryService rentalSpotQueryService;
    private final RentalSpotService rentalSpotService;
    private final RentalSpotMapper rentalSpotMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_spot:view')")
    public ResponseEntity<List<RentalSpotHierarchyResponse>> getRentalSpotsHierarchy() {
        return ResponseEntity.ok(rentalSpotQueryService.getAllRentalSpotsHierarchy());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_spot:view')")
    public ResponseEntity<RentalSpotHierarchyResponse> getRentalSpotHierarchy(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(rentalSpotQueryService.getRentalSpotHierarchyUp(id));
    }

    @GetMapping("/{id}/scooters")
    @PreAuthorize("hasAuthority('SCOPE_spot:view')")
    public ResponseEntity<RentalSpotWithScootersResponse> getRentalSpotWithScooters(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(rentalSpotQueryService.getRentalSpotScooters(id));
    }

    @GetMapping("/{id}/info")
    @PreAuthorize("hasAuthority('SCOPE_spot:view_admin')")
    public ResponseEntity<RentalSpotDetailedResponse> getRentalSpotDetailedInfo(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(rentalSpotQueryService.getRentalSpotsDetailedInfo(id));
    }

    @GetMapping(params = {"latitude", "longitude", "radius"})
    @PreAuthorize("hasAuthority('SCOPE_spot:view')")
    public ResponseEntity<List<RentalSpotConciseResponse>> getNearbyRentalSpots(
            @RequestParam float latitude,
            @RequestParam float longitude,
            @RequestParam float radius
    ) {
        return ResponseEntity.ok(rentalSpotQueryService.getNearbySpots(
                GeoSearchParams.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .radius(radius)
                        .build()
        ));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_spot:manage')")
    public ResponseEntity<RentalSpotDetailedResponse> postRentalSpot(
        @Valid @RequestBody CreateRentalSpotRequest request
    ) {
        RentalSpotDetailedResponse result = rentalSpotService.openPoint(
                rentalSpotMapper.toCreateCommand(request)
        );
        URI location = URI.create("/api/rental-spots/" + result.getId());

        return ResponseEntity
                .created(location)
                .body(result);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_spot:manage')")
    public ResponseEntity<RentalSpotDetailedResponse> patchRentalSpot(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateRentalSpotRequest request
    ) {
        RentalSpotDetailedResponse result = rentalSpotService.updatePointInfo(
                id,
                rentalSpotMapper.toUpdateCommand(request)
        );
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_spot:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRentalSpot(
            @PathVariable Integer id
    ) {
        rentalSpotService.closeSpot(id);
    }
}
