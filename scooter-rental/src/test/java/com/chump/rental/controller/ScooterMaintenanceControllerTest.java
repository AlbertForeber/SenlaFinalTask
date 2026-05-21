package com.chump.rental.controller;

import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.rental.service.ScooterService;
import com.chump.common.config.TestConfig;
import com.chump.common.config.WithMockUserId;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        ScooterMaintenanceController.class,
        ScooterMaintenanceControllerTest.Config.class,
        JwtAuthenticationEntryPoint.class,
        RestExceptionHandler.class,
        TestConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.expiration-time=100000",
        "auth.jwt.secret-key=SECRETKEYSECRETKEYSECRETKEYSECRETKEY"
})
@WebAppConfiguration
@DisplayName("Scooter maintenance controller testing")
public class ScooterMaintenanceControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired ScooterService scooterService;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(scooterService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public ScooterService scooterService() {
            return Mockito.mock(ScooterService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Begin maintenance endpoint should return unauthorized error response, if auth hasn't 'scooter:maintenance' scope")
    public void beginMaintenanceShouldReturnErrorWhenNoScooterMaintenance() throws Exception {
        mockMvc.perform(post("/api/scooters/1/maintenance/start"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Begin maintenance endpoint should return ok, if auth has 'scooter:maintenance' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:maintenance")
    public void beginMaintenanceShouldReturnOkWhenScooterMaintenance() throws Exception {
        mockMvc.perform(post("/api/scooters/1/maintenance/start"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Replace battery endpoint should return unauthorized error response, if auth hasn't 'scooter:maintenance' scope")
    public void replaceBatteryShouldReturnErrorWhenNoScooterMaintenance() throws Exception {
        mockMvc.perform(post("/api/scooters/1/maintenance/replace-battery"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Replace battery endpoint should return ok, if auth has 'scooter:maintenance' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:maintenance")
    public void replaceBatteryShouldReturnOkWhenScooterMaintenance() throws Exception {
        mockMvc.perform(post("/api/scooters/1/maintenance/replace-battery"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Finish maintenance endpoint should return unauthorized error response, if auth hasn't 'scooter:maintenance' scope")
    public void finishMaintenanceShouldReturnErrorWhenNoScooterMaintenance() throws Exception {
        mockMvc.perform(post("/api/scooters/1/maintenance/finish"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Finish maintenance endpoint should return ok, if auth has 'scooter:maintenance' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:maintenance")
    public void finishMaintenanceShouldReturnOkWhenScooterMaintenance() throws Exception {
        mockMvc.perform(post("/api/scooters/1/maintenance/finish"))
                .andExpect(status().isOk());
    }
}