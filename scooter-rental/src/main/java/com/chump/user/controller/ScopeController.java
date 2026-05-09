package com.chump.user.controller;

import com.chump.user.dto.response.ScopeResponse;
import com.chump.user.service.query.ScopeQueryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scopes")
@Validated
@RequiredArgsConstructor
public class ScopeController {

    private final ScopeQueryService scopeQueryService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_role:manage')")
    public ResponseEntity<List<ScopeResponse>> getScopes(
            @RequestParam(defaultValue = "10", required = false)
            @Positive(message = "Param 'pageSize' must be positive number")
            int pageSize,

            @RequestParam(defaultValue = "1", required = false)
            @Positive(message = "Param 'page' must be positive number")
            int page
    ) {
        return ResponseEntity.ok(scopeQueryService.getAllScopes(pageSize, page));
    }
}
