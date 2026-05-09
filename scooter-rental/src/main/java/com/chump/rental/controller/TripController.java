package com.chump.rental.controller;

import com.chump.rental.dto.request.RefundTripRequest;
import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.dto.response.TripDetailedResponse;
import com.chump.rental.dto.response.TripPointResponse;
import com.chump.rental.dto.response.TripRefundResponse;
import com.chump.rental.service.TripService;
import com.chump.rental.service.query.TripPointQueryService;
import com.chump.rental.service.query.TripQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@Validated
@RequiredArgsConstructor
public class TripController {

    private final TripQueryService tripQueryService;
    private final TripPointQueryService tripPointQueryService;
    private final TripService tripService;

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

    @GetMapping("/{id}/points")
    @PreAuthorize("hasAuthority('SCOPE_trip:view_admin')")
    public ResponseEntity<List<TripPointResponse>> getTripPoints(
            @PathVariable Integer id,

            @Positive(message = "Param 'pageSize' must not be negative")
            @RequestParam(defaultValue = "10", required = false) int pageSize,

            @Positive(message = "Param 'page' must not be negative")
            @RequestParam(defaultValue = "1", required = false) int page
    ) {
        return ResponseEntity.ok(tripPointQueryService.getTripPoints(id, pageSize, page));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('SCOPE_trip:manage_admin')")
    public ResponseEntity<TripRefundResponse> postRefund(
            @PathVariable Integer id,
            @Valid @RequestBody RefundTripRequest request
    ) {
        return ResponseEntity.ok(tripService.refundTrip(id, request.getForLastSeconds()));
    }
}
