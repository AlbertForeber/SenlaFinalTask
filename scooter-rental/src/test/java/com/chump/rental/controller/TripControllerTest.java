package com.chump.rental.controller;

import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.rental.dto.request.RefundTripRequest;
import com.chump.rental.dto.response.TripDetailedResponse;
import com.chump.rental.service.TripService;
import com.chump.rental.service.query.TripPointQueryService;
import com.chump.rental.service.query.TripQueryService;
import com.chump.common.config.TestConfig;
import com.chump.common.config.WithMockUserId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
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
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        TripController.class,
        TripControllerTest.Config.class,
        JwtAuthenticationEntryPoint.class,
        RestExceptionHandler.class,
        TestConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.expiration-time=100000",
        "auth.jwt.secret-key=SECRETKEYSECRETKEYSECRETKEYSECRETKEY"
})
@WebAppConfiguration
@DisplayName("Trip controller testing")
public class TripControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired TripQueryService tripQueryService;
    @Autowired TripPointQueryService tripPointQueryService;
    @Autowired TripService tripService;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(tripQueryService, tripPointQueryService, tripService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public TripQueryService tripQueryService() {
            return Mockito.mock(TripQueryService.class);
        }

        @Bean
        public TripPointQueryService tripPointQueryService() {
            return Mockito.mock(TripPointQueryService.class);
        }

        @Bean
        public TripService tripService() {
            return Mockito.mock(TripService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Get current trips endpoint should return unauthorized error response, if auth hasn't 'trip:view' scope")
    public void getCurrentTripsShouldReturnErrorWhenNoTripView() throws Exception {
        mockMvc.perform(get("/api/trips/current"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tripQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get current trips endpoint should return ok, if auth has 'trip:view' scope")
    @WithMockUserId(scopes = "SCOPE_trip:view")
    public void getCurrentTripsShouldReturnOkWhenTripView() throws Exception {
        mockMvc.perform(get("/api/trips/current"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trips from period endpoint should return unauthorized error response, if auth hasn't 'trip:view_period' scope")
    public void getTripsFromPeriodShouldReturnErrorWhenNoTripViewPeriod() throws Exception {
        mockMvc.perform(get("/api/trips")
                        .param("from", "2024-01-01T00:00:00Z")
                        .param("to", "2024-12-31T23:59:59Z"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tripQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trips from period endpoint should return ok, if auth has 'trip:view_period' scope")
    @WithMockUserId(scopes = "SCOPE_trip:view_period")
    public void getTripsFromPeriodShouldReturnOkWhenTripViewPeriod() throws Exception {
        mockMvc.perform(get("/api/trips")
                        .param("from", "2024-01-01T00:00:00Z")
                        .param("to", "2024-12-31T23:59:59Z"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip endpoint should return unauthorized error response, if auth hasn't 'trip:view' scope")
    public void getTripShouldReturnErrorWhenNoTripView() throws Exception {
        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tripQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip endpoint should return ok, if auth has 'trip:view' scope and trip belongs to user")
    @WithMockUserId(value = 42, scopes = "SCOPE_trip:view")
    public void getTripShouldReturnOkWhenTripView() throws Exception {
        TripDetailedResponse response = new TripDetailedResponse();
        response.setUserId(42);
        when(tripQueryService.getTripInfo(1)).thenReturn(response);

        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip endpoint should return forbidden, if auth has only 'trip:view' scope and trip belongs to another user")
    @WithMockUserId(value = 99, scopes = "SCOPE_trip:view")
    public void getTripShouldReturnForbiddenWhenTripBelongsToAnotherUser() throws Exception {
        TripDetailedResponse response = new TripDetailedResponse();
        response.setUserId(42);
        when(tripQueryService.getTripInfo(1)).thenReturn(response);

        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip endpoint should return ok, if auth has 'trip:view_admin' scope regardless of trip owner")
    @WithMockUserId(value = 99, scopes = {"SCOPE_trip:view_admin", "SCOPE_trip:view"})
    public void getTripShouldReturnOkWhenTripViewAdmin() throws Exception {
        TripDetailedResponse response = new TripDetailedResponse();
        response.setUserId(42);
        when(tripQueryService.getTripInfo(1)).thenReturn(response);

        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip points endpoint should return unauthorized error response, if auth hasn't 'trip:view_admin' scope")
    public void getTripPointsShouldReturnErrorWhenNoTripViewAdmin() throws Exception {
        mockMvc.perform(get("/api/trips/1/points"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tripPointQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip points endpoint should return ok, if auth has 'trip:view_admin' scope")
    @WithMockUserId(scopes = "SCOPE_trip:view_admin")
    public void getTripPointsShouldReturnOkWhenTripViewAdmin() throws Exception {
        mockMvc.perform(get("/api/trips/1/points"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip points endpoint should return validation exception error response, if page is not positive")
    @WithMockUserId(scopes = "SCOPE_trip:view_admin")
    public void getTripPointsShouldReturnErrorWhenPageIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/trips/1/points")
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("page")));

        verifyNoInteractions(tripPointQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip points endpoint should return validation exception error response, if page size is not positive")
    @WithMockUserId(scopes = "SCOPE_trip:view_admin")
    public void getTripPointsShouldReturnErrorWhenPageSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/trips/1/points")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("pageSize")));

        verifyNoInteractions(tripPointQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post refund endpoint should return unauthorized error response, if auth hasn't 'trip:manage_admin' scope")
    public void postRefundShouldReturnErrorWhenNoTripManageAdmin() throws Exception {
        RefundTripRequest request = new RefundTripRequest();
        request.setForLastSeconds(60);

        mockMvc.perform(post("/api/trips/1/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tripService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post refund endpoint should return ok, if auth has 'trip:manage_admin' scope")
    @WithMockUserId(scopes = "SCOPE_trip:manage_admin")
    public void postRefundShouldReturnOkWhenTripManageAdmin() throws Exception {
        RefundTripRequest request = new RefundTripRequest();
        request.setForLastSeconds(60);

        mockMvc.perform(post("/api/trips/1/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Post refund endpoint should return validation exception error response, if forLastSeconds is negative")
    @WithMockUserId(scopes = "SCOPE_trip:manage_admin")
    public void postRefundShouldReturnErrorWhenForLastSecondsIsNegative() throws Exception {
        RefundTripRequest request = new RefundTripRequest();
        request.setForLastSeconds(-1);

        mockMvc.perform(post("/api/trips/1/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("forLastSeconds")));

        verifyNoInteractions(tripService);
    }
}