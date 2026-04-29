package com.chump.common.security;

import com.chump.auth.service.JwtService;
import com.chump.common.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        int userId;
        Collection<? extends GrantedAuthority> scopes;

        try {
            userId = jwtService.getUserId(jwt);
            scopes = jwtService.getScopes(jwt);
        } catch (Exception e) {

            // Единственный доступный способ отлова ошибок в фильтре
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(401)
                    .error("Invalid token")
                    .message("Error during token validation")
                    .details(Collections.singletonList(e.getMessage()))
                    .build();

            String json = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(json);
            return;
        }

        // Проверяем что пользователь еще не аутентифицирован
        // На случай добавления дополнительных способов аутентификации
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    scopes
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        }
    }
}
