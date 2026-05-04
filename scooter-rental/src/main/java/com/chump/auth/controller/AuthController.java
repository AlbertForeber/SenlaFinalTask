package com.chump.auth.controller;

import com.chump.auth.dto.request.LoginRequest;
import com.chump.auth.dto.request.LogoutRequest;
import com.chump.auth.dto.request.RefreshRequest;
import com.chump.auth.dto.request.RegisterRequest;
import com.chump.auth.dto.response.TokenResponse;
import com.chump.auth.mapper.AuthMapper;
import com.chump.auth.service.AuthFacade;
import com.chump.common.utils.DeviceInfoResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;
    private final AuthMapper authMapper;
    private final DeviceInfoResolver deviceInfoResolver;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authFacade.login(
                authMapper.toLoginCommand(
                        request,
                        deviceInfoResolver.resolveDeviceName(httpRequest.getHeader("User-Agent")),
                        deviceInfoResolver.resolveIpAddress(httpRequest)
                )
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authFacade.register(
                authMapper.toRegisterCommand(
                        request,
                        deviceInfoResolver.resolveDeviceName(httpRequest.getHeader("User-Agent")),
                        deviceInfoResolver.resolveIpAddress(httpRequest)
                )
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        return ResponseEntity.ok(authFacade.refresh(
                request.getOldRefreshToken()
        ));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @AuthenticationPrincipal Integer userId,
            @Valid @RequestBody LogoutRequest request
    ) {
        authFacade.logout(userId, request.getRefreshToken());
    }
}
