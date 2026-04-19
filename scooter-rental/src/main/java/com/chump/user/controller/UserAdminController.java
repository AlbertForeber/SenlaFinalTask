package com.chump.user.controller;

import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.service.query.TripQueryService;
import com.chump.user.dto.request.UpdateUserProtectedInfoRequest;
import com.chump.user.dto.request.UpdateUserRoleRequest;
import com.chump.user.dto.response.UserProfileResponse;
import com.chump.user.dto.response.UserRoleResponse;
import com.chump.user.mapper.UserMapper;
import com.chump.user.service.UserService;
import com.chump.user.service.query.UserQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/{id}")
public class UserAdminController {

    private final UserService userService;
    private final UserQueryService userQueryService;
    private final TripQueryService tripQueryService;
    private final UserMapper userMapper;

    public UserAdminController(UserService userService, UserQueryService userQueryService, TripQueryService tripQueryService, UserMapper userMapper) {
        this.userService = userService;
        this.userQueryService = userQueryService;
        this.tripQueryService = tripQueryService;
        this.userMapper = userMapper;
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('SCOPE_profile:view_admin')")
    public ResponseEntity<List<TripConciseResponse>> getUserTripHistory(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(tripQueryService.getUserTrips(id));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('SCOPE_profile:view_admin')")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(userQueryService.getUserProfile(id));
    }

    @PatchMapping("/profile")
    @PreAuthorize("hasAuthority('SCOPE_profile:manage_admin')")
    public ResponseEntity<UserProfileResponse> patchUserProfile(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserProtectedInfoRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserProtectedInfo(
                id,
                userMapper.toProtectedInfoCommand(request)
        ));
    }

    @DeleteMapping("/profile")
    @PreAuthorize("hasAuthority('SCOPE_profile:manage_admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserProfile(
            @PathVariable Integer id
    ) {
        userService.deleteUser(id);
    }

    @PatchMapping("/role")
    @PreAuthorize("hasAuthority('SCOPE_profile:manage_role')")
    public ResponseEntity<UserRoleResponse> patchUserRole(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserRole(id, request.getNewRoleId()));
    }
}


