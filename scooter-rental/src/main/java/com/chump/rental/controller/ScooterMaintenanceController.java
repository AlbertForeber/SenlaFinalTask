package com.chump.rental.controller;

import com.chump.rental.dto.response.ScooterResponse;
import com.chump.rental.service.ScooterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scooters/{id}/maintenance")
public class ScooterMaintenanceController {

    private final ScooterService scooterService;

    public ScooterMaintenanceController(ScooterService scooterService) {
        this.scooterService = scooterService;
    }

    @PatchMapping("/start")
    @PreAuthorize("hasAuthority('SCOPE_scooter:maintenance')")
    public ResponseEntity<ScooterResponse> beginMaintenance(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(scooterService.beginMaintenance(id));
    }

    @PatchMapping("/replace-battery")
    @PreAuthorize("hasAuthority('SCOPE_scooter:maintenance')")
    public ResponseEntity<ScooterResponse> replaceBattery(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(scooterService.rechargeScooterBattery(id));
    }

    @PatchMapping("/finish")
    @PreAuthorize("hasAuthority('SCOPE_scooter:maintenance')")
    public ResponseEntity<ScooterResponse> finishMaintenance(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(scooterService.finishMaintenance(id));
    }
}