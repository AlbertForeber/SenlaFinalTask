package com.chump.user.controller;

import com.chump.rental.dto.response.TripConciseResponse;
import com.chump.rental.service.query.TripQueryService;
import com.chump.user.dto.request.UpdateUserBaseInfoRequest;
import com.chump.user.dto.response.UserProfileResponse;
import com.chump.user.mapper.UserMapper;
import com.chump.user.service.UserService;
import com.chump.user.service.query.UserQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final TripQueryService tripQueryService;
    private final UserQueryService userQueryService;
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(TripQueryService tripQueryService, UserQueryService userQueryService, UserService userService, UserMapper userMapper) {
        this.tripQueryService = tripQueryService;
        this.userQueryService = userQueryService;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('SCOPE_profile:view')")
    public ResponseEntity<List<TripConciseResponse>> getUserTripHistory(
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(tripQueryService.getUserTrips(userId));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('SCOPE_profile:view')")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(userQueryService.getUserProfile(userId));
    }

    @PatchMapping("/profile")
    @PreAuthorize("hasAuthority('SCOPE_profile:manage')")
    public ResponseEntity<UserProfileResponse> patchUserProfile(
            @AuthenticationPrincipal Integer userId,
            @Valid @RequestBody UpdateUserBaseInfoRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserBaseInfo(
                userId,
                userMapper.toBaseInfoCommand(request)
        ));
    }

    @DeleteMapping("/profile")
    @PreAuthorize("hasAuthority('SCOPE_profile:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserProfile(
            @AuthenticationPrincipal Integer userId
    ) {
        userService.deleteUser(userId);
    }
}