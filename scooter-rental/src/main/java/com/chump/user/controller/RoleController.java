package com.chump.user.controller;

import com.chump.user.dto.request.CreateRoleRequest;
import com.chump.user.dto.request.UpdateRoleRequest;
import com.chump.user.dto.response.RoleResponse;
import com.chump.user.dto.response.RoleWithScopesResponse;
import com.chump.user.mapper.RoleMapper;
import com.chump.user.service.RoleService;
import com.chump.user.service.query.RoleQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleQueryService roleQueryService;
    private final RoleService roleService;
    private final RoleMapper roleMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_role:view')")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(roleQueryService.getAllRoles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_role:view')")
    public ResponseEntity<RoleWithScopesResponse> getRoleWithScopes(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(roleQueryService.getRoleInfo(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_role:manage')")
    public ResponseEntity<RoleWithScopesResponse> postRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        RoleWithScopesResponse result = roleService.addRole(
                roleMapper.toCreateCommand(request)
        );
        URI uri = URI.create("/api/roles/" + result.getId());

        return ResponseEntity
                .created(uri)
                .body(result);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_role:manage')")
    public ResponseEntity<RoleWithScopesResponse> patchRole(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(roleService.updateRole(
                id,
                roleMapper.toUpdateCommand(request)
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_role:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(
            @PathVariable Integer id
    ) {
        roleService.deleteRole(id);
    }
}
