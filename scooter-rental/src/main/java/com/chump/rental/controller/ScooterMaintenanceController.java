package com.chump.rental.controller;

import com.chump.rental.dto.response.ScooterResponse;
import com.chump.rental.service.ScooterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scooters/{id}/maintenance")
@RequiredArgsConstructor
public class ScooterMaintenanceController {

    private final ScooterService scooterService;

    @PostMapping("/start")
    @PreAuthorize("hasAuthority('SCOPE_scooter:maintenance')")
    public ResponseEntity<ScooterResponse> beginMaintenance(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(scooterService.beginMaintenance(id));
    }

    @PostMapping("/replace-battery")
    @PreAuthorize("hasAuthority('SCOPE_scooter:maintenance')")
    public ResponseEntity<ScooterResponse> replaceBattery(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(scooterService.rechargeScooterBattery(id));
    }

    @PostMapping("/finish")
    @PreAuthorize("hasAuthority('SCOPE_scooter:maintenance')")
    public ResponseEntity<ScooterResponse> finishMaintenance(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(scooterService.finishMaintenance(id));
    }
}