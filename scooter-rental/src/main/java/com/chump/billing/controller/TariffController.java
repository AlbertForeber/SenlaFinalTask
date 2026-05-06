package com.chump.billing.controller;

import com.chump.billing.dto.request.CreateTariffRequest;
import com.chump.billing.dto.request.UpdateTariffRequest;
import com.chump.billing.dto.response.TariffConciseResponse;
import com.chump.billing.dto.response.TariffDetailedResponse;
import com.chump.billing.mapper.TariffMapper;
import com.chump.billing.service.TariffService;
import com.chump.billing.service.query.TariffQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
@Validated
@RequiredArgsConstructor
public class TariffController {

    private final TariffQueryService tariffQueryService;
    private final TariffService tariffService;
    private final TariffMapper tariffMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_tariff:view')")
    public ResponseEntity<List<TariffConciseResponse>> getTariffs(
            @Positive(message = "Param 'pageSize' must not be negative")
            @RequestParam(defaultValue = "10", required = false) int pageSize,

            @Positive(message = "Param 'page' must not be negative")
            @RequestParam(defaultValue = "1", required = false) int page
    ) {
        return ResponseEntity.ok(tariffQueryService.getAllTariffs(pageSize, page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_tariff:view')")
    public ResponseEntity<TariffDetailedResponse> getTariff(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(tariffQueryService.getTariffById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_tariff:manage')")
    public ResponseEntity<TariffDetailedResponse> patchTariff(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateTariffRequest request
    ) {
        return ResponseEntity.ok(tariffService.updateTariff(
                id,
                tariffMapper.toUpdateCommand(request)
        ));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_tariff:manage')")
    public ResponseEntity<TariffDetailedResponse> postTariff(
            @Valid @RequestBody CreateTariffRequest request
    ) {
        TariffDetailedResponse result = tariffService.addTariff(tariffMapper.toCreateCommand(request));
        URI uri = URI.create("/api/tariffs/" + result.getId());

        return ResponseEntity
                .created(uri)
                .body(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_tariff:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTariff(
            @PathVariable Integer id
    ) {
        tariffService.deleteTariff(id);
    }
}
