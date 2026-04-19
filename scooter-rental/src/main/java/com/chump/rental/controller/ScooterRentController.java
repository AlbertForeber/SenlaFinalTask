package com.chump.rental.controller;

import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.dto.response.TripDetailedResponse;
import com.chump.rental.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scooters/{id}")
public class ScooterRentController {

    private final RentalService rentalService;

    public ScooterRentController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PatchMapping("/rent")
    @PreAuthorize("hasAuthority('SCOPE_scooter:rent')")
    public ResponseEntity<TripConciseResponse> rentScooter(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(rentalService.rentScooter(id, userId));
    }

    @PatchMapping("/return")
    @PreAuthorize("hasAuthority('SCOPE_scooter:rent')")
    public ResponseEntity<TripDetailedResponse> returnScooter(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(rentalService.returnScooter(id, userId, false));
    }

    @PatchMapping("/pause")
    @PreAuthorize("hasAuthority('SCOPE_scooter:rent')")
    public ResponseEntity<TripConciseResponse> pauseScooter(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(rentalService.pauseScooter(id, userId));
    }

    @PatchMapping("/resume")
    @PreAuthorize("hasAuthority('SCOPE_scooter:rent')")
    public ResponseEntity<TripConciseResponse> resumeScooter(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(rentalService.resumeScooter(id, userId));
    }

    @PatchMapping("/force-stop")
    @PreAuthorize("hasAuthority('SCOPE_scooter:manage')")
    public ResponseEntity<TripDetailedResponse> forceStopScooter(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(rentalService.returnScooter(id, userId, true));
    }
}
