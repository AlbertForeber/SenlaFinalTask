package com.chump.rental.controller;

import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.dto.response.TripDetailedResponse;
import com.chump.rental.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scooters/{id}")
@RequiredArgsConstructor
public class ScooterRentController {

    private final RentalService rentalService;

    @PostMapping("/rent")
    @PreAuthorize("hasAuthority('SCOPE_scooter:rent')")
    public ResponseEntity<TripConciseResponse> rentScooter(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(rentalService.rentScooter(id, userId));
    }

    @PostMapping("/return")
    @PreAuthorize("hasAuthority('SCOPE_scooter:rent')")
    public ResponseEntity<TripDetailedResponse> returnScooter(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(rentalService.returnScooter(id, userId, false));
    }

    @PostMapping("/pause")
    @PreAuthorize("hasAuthority('SCOPE_scooter:rent')")
    public ResponseEntity<TripConciseResponse> pauseScooter(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(rentalService.pauseScooter(id, userId));
    }

    @PostMapping("/resume")
    @PreAuthorize("hasAuthority('SCOPE_scooter:rent')")
    public ResponseEntity<TripConciseResponse> resumeScooter(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(rentalService.resumeScooter(id, userId));
    }

    @PostMapping("/force-stop")
    @PreAuthorize("hasAuthority('SCOPE_scooter:manage')")
    public ResponseEntity<TripDetailedResponse> forceStopScooter(
            @PathVariable Integer id,
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(rentalService.returnScooter(id, userId, true));
    }
}
