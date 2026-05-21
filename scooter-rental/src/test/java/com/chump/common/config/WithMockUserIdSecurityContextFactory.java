package com.chump.common.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;

public class WithMockUserIdSecurityContextFactory
        implements WithSecurityContextFactory<WithMockUserId> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserId annotation) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        annotation.value(),
                        null,
                        Arrays.stream(annotation.scopes()).map(
                                SimpleGrantedAuthority::new
                        ).toList()
                );

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);

        return context;
    }
}
