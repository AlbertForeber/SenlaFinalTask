package com.chump.auth.controller;

import com.chump.auth.service.SessionService;
import com.chump.auth.service.query.SessionQueryService;
import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.test_config.TestConfig;
import com.chump.test_config.WithMockUserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        SessionController.class,
        SessionControllerTest.Config.class,
        JwtAuthenticationEntryPoint.class,
        RestExceptionHandler.class,
        TestConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.expiration-time=100000",
        "auth.jwt.secret-key=SECRETKEYSECRETKEYSECRETKEYSECRETKEY"
})
@WebAppConfiguration
@DisplayName("Session controller testing")
public class SessionControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    SessionQueryService sessionQueryService;

    @Autowired
    SessionService sessionService;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(sessionQueryService);
        Mockito.reset(sessionService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public SessionQueryService sessionQueryService() {
            return Mockito.mock(SessionQueryService.class);
        }

        @Bean
        public SessionService sessionService() {
            return Mockito.mock(SessionService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user sessions endpoint should return unauthorized error response, if auth hasn't 'session:view' scope")
    public void getUserSessionShouldReturnErrorWhenNoSessionView() throws Exception {
        mockMvc.perform(get("/api/auth/sessions"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sessionQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user sessions endpoint should return ok, if auth has 'session:view' scope")
    @WithMockUserId(scopes = "SCOPE_session:view")
    public void getUserSessionShouldReturnErrorWhenSessionView() throws Exception {
        mockMvc.perform(get("/api/auth/sessions"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete user sessions endpoint should return unauthorized error response, if auth hasn't 'session:manage' scope")
    public void deleteAllSessionsShouldReturnErrorWhenNoSessionManage() throws Exception {
        mockMvc.perform(delete("/api/auth/sessions"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sessionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete user sessions endpoint should return ok, if auth has 'session:manage' scope")
    @WithMockUserId(scopes = "SCOPE_session:manage")
    public void deleteAllSessionsSessionShouldReturnErrorWhenSessionManage() throws Exception {
        mockMvc.perform(delete("/api/auth/sessions"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete session endpoint should return unauthorized error response, if auth hasn't 'session:manage' scope")
    public void deleteSessionShouldReturnErrorWhenNoSessionManage() throws Exception {
        mockMvc.perform(delete("/api/auth/sessions/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sessionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete session endpoint should return ok, if auth has 'session:manage' scope")
    @WithMockUserId(scopes = "SCOPE_session:manage")
    public void deleteSessionSessionShouldReturnErrorWhenSessionManage() throws Exception {
        mockMvc.perform(delete("/api/auth/sessions/1"))
                .andExpect(status().isNoContent());
    }
}