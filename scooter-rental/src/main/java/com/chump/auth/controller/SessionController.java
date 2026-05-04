package com.chump.auth.controller;

import com.chump.auth.dto.response.SessionResponse;
import com.chump.auth.service.SessionService;
import com.chump.auth.service.query.SessionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionQueryService sessionQueryService;
    private final SessionService sessionService;

    @GetMapping
    @PreAuthorize("hasAuthority('session:view')")
    public ResponseEntity<List<SessionResponse>> getUserSessions(
            @AuthenticationPrincipal Integer userId
    ) {
        return ResponseEntity.ok(sessionQueryService.getAllUserSessions(userId));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('session:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllSessions(
            @AuthenticationPrincipal Integer userId
    ) {
        sessionService.terminateAllUserSessions(userId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('session:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(
            @AuthenticationPrincipal Integer userId,
            @PathVariable Integer id
    ) {
        sessionService.terminateSession(id, userId);
    }
}
