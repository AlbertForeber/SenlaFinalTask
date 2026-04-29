package com.chump.auth.controller;

import com.chump.auth.dto.request.LoginRequest;
import com.chump.auth.dto.request.RefreshRequest;
import com.chump.auth.dto.request.RegisterRequest;
import com.chump.auth.dto.response.TokenResponse;
import com.chump.auth.mapper.AuthMapper;
import com.chump.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthMapper authMapper;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(
                authMapper.toLoginCommand(request)
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(
                authMapper.toRegisterCommand(request)
        ));
    }

    @PostMapping("refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(
                request.getOldRefreshToken()
        ));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @AuthenticationPrincipal Integer userId
    ) {
        authService.logout(userId);
    }
}
