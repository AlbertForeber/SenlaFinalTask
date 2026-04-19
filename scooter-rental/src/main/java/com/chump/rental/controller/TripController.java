package com.chump.rental.controller;

import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.dto.response.TripDetailedResponse;
import com.chump.rental.service.query.TripQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripQueryService tripQueryService;

    public TripController(TripQueryService tripQueryService) {
        this.tripQueryService = tripQueryService;
    }

    @GetMapping("/current")
    @PreAuthorize("hasAuthority('SCOPE_trip:view')")
    public ResponseEntity<List<TripConciseResponse>> getCurrentTrips(
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(tripQueryService.getOngoingTrips(userId));
    }

    @GetMapping(params = {"from", "to"})
    @PreAuthorize("hasAuthority('SCOPE_trip:view_period')")
    public ResponseEntity<List<TripConciseResponse>> getTripsFromPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return ResponseEntity.ok(tripQueryService.getTripsFromPeriod(from, to));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_trip:view')")
    @PostAuthorize("hasAuthority('SCOPE_trip:view_admin') || returnObject.body.userId == authentication.principal")
    public ResponseEntity<TripDetailedResponse> getTrip(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(tripQueryService.getTripInfo(id));
    }
}
