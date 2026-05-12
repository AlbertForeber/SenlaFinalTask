package com.chump.rental.controller;

import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.rental.dto.request.CreateScooterRequest;
import com.chump.rental.dto.request.UpdateScooterInfoRequest;
import com.chump.rental.dto.response.ScooterResponse;
import com.chump.rental.mapper.ScooterMapperImpl;
import com.chump.rental.mapper.ScooterModelMapperImpl;
import com.chump.rental.model.status.ScooterStatus;
import com.chump.rental.service.ScooterService;
import com.chump.rental.service.query.ScooterQueryService;
import com.chump.rental.service.query.TripQueryService;
import com.chump.test_config.TestConfig;
import com.chump.test_config.WithMockUserId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        ScooterController.class,
        ScooterControllerTest.Config.class,
        ScooterMapperImpl.class,
        ScooterModelMapperImpl.class,
        JwtAuthenticationEntryPoint.class,
        RestExceptionHandler.class,
        TestConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.expiration-time=100000",
        "auth.jwt.secret-key=SECRETKEYSECRETKEYSECRETKEYSECRETKEY"
})
@WebAppConfiguration
@DisplayName("Scooter controller testing")
class ScooterControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired ScooterQueryService scooterQueryService;
    @Autowired ScooterService scooterService;
    @Autowired TripQueryService tripQueryService;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(scooterQueryService, scooterService, tripQueryService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public ScooterQueryService scooterQueryService() {
            return Mockito.mock(ScooterQueryService.class);
        }

        @Bean
        public ScooterService scooterService() {
            return Mockito.mock(ScooterService.class);
        }

        @Bean
        public TripQueryService tripQueryService() {
            return Mockito.mock(TripQueryService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooters endpoint should return unauthorized error response, if auth hasn't 'scooter:view' scope")
    public void getAllFreeScootersShouldReturnErrorWhenNoScooterView() throws Exception {
        mockMvc.perform(get("/api/scooters"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooters endpoint should return ok, if auth has 'scooter:view' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:view")
    public void getAllFreeScootersShouldReturnOkWhenScooterView() throws Exception {
        mockMvc.perform(get("/api/scooters"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooters endpoint should return validation exception error response, if page is not positive")
    @WithMockUserId(scopes = "SCOPE_scooter:view")
    public void getAllFreeScootersShouldReturnErrorWhenPageIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("page")));

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooters endpoint should return validation exception error response, if page size is not positive")
    @WithMockUserId(scopes = "SCOPE_scooter:view")
    public void getAllFreeScootersShouldReturnErrorWhenPageSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("pageSize")));

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get nearby scooters endpoint should return unauthorized error response, if auth hasn't 'scooter:view' scope")
    public void getNearbyFreeScootersShouldReturnErrorWhenNoScooterView() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("latitude", "55.0")
                        .param("longitude", "37.0")
                        .param("radius", "500"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get nearby scooters endpoint should return ok, if auth has 'scooter:view' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:view")
    public void getNearbyFreeScootersShouldReturnOkWhenScooterView() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("latitude", "55.0")
                        .param("longitude", "37.0")
                        .param("radius", "500"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get nearby scooters endpoint should return validation exception error response, if latitude is out of range")
    @WithMockUserId(scopes = "SCOPE_scooter:view")
    public void getNearbyFreeScootersShouldReturnErrorWhenLatitudeOutOfRange() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("latitude", "91.0")
                        .param("longitude", "37.0")
                        .param("radius", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("latitude")));

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get nearby scooters endpoint should return validation exception error response, if longitude is out of range")
    @WithMockUserId(scopes = "SCOPE_scooter:view")
    public void getNearbyFreeScootersShouldReturnErrorWhenLongitudeOutOfRange() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("latitude", "55.0")
                        .param("longitude", "181.0")
                        .param("radius", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("longitude")));

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get nearby scooters endpoint should return validation exception error response, if radius is not positive")
    @WithMockUserId(scopes = "SCOPE_scooter:view")
    public void getNearbyFreeScootersShouldReturnErrorWhenRadiusIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("latitude", "55.0")
                        .param("longitude", "37.0")
                        .param("radius", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("radius")));

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooters by status endpoint should return unauthorized error response, if auth hasn't 'scooter:view_by_status' scope")
    public void getScooterWithStatusShouldReturnErrorWhenNoScooterViewByStatus() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("status", "FREE"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooters by status endpoint should return ok, if auth has 'scooter:view_by_status' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:view_by_status")
    public void getScooterWithStatusShouldReturnOkWhenScooterViewByStatus() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("status", "FREE"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooters by status endpoint should return validation exception error response, if page is not positive")
    @WithMockUserId(scopes = "SCOPE_scooter:view_by_status")
    public void getScooterWithStatusShouldReturnErrorWhenPageIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("status", "FREE")
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("page")));

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooters by status endpoint should return validation exception error response, if page size is not positive")
    @WithMockUserId(scopes = "SCOPE_scooter:view_by_status")
    public void getScooterWithStatusShouldReturnErrorWhenPageSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/scooters")
                        .param("status", "FREE")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("pageSize")));

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooter models endpoint should return unauthorized error response, if auth hasn't 'scooter:view_admin' scope")
    public void getScooterModelsShouldReturnErrorWhenNoScooterViewAdmin() throws Exception {
        mockMvc.perform(get("/api/scooters/models"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooter models endpoint should return ok, if auth has 'scooter:view_admin' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:view_admin")
    public void getScooterModelsShouldReturnOkWhenScooterViewAdmin() throws Exception {
        mockMvc.perform(get("/api/scooters/models"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooter trips endpoint should return unauthorized error response, if auth hasn't 'scooter:view_admin' scope")
    public void getScooterHistoryShouldReturnErrorWhenNoScooterViewAdmin() throws Exception {
        mockMvc.perform(get("/api/scooters/1/trips"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tripQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooter trips endpoint should return ok, if auth has 'scooter:view_admin' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:view_admin")
    public void getScooterHistoryShouldReturnOkWhenScooterViewAdmin() throws Exception {
        mockMvc.perform(get("/api/scooters/1/trips"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooter trips endpoint should return validation exception error response, if page is not positive")
    @WithMockUserId(scopes = "SCOPE_scooter:view_admin")
    public void getScooterHistoryShouldReturnErrorWhenPageIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/scooters/1/trips")
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("page")));

        verifyNoInteractions(tripQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooter trips endpoint should return validation exception error response, if page size is not positive")
    @WithMockUserId(scopes = "SCOPE_scooter:view_admin")
    public void getScooterHistoryShouldReturnErrorWhenPageSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/scooters/1/trips")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("pageSize")));

        verifyNoInteractions(tripQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooter endpoint should return unauthorized error response, if auth hasn't 'scooter:view' scope")
    public void getScooterInfoShouldReturnErrorWhenNoScooterView() throws Exception {
        mockMvc.perform(get("/api/scooters/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooter endpoint should return ok, if auth has 'scooter:view' scope and scooter is free")
    @WithMockUserId(scopes = "SCOPE_scooter:view")
    public void getScooterInfoShouldReturnOkWhenScooterViewAndScooterFree() throws Exception {
        ScooterResponse response = new ScooterResponse();
        response.setStatus(ScooterStatus.FREE);
        when(scooterQueryService.getScooterInfo(1)).thenReturn(response);

        mockMvc.perform(get("/api/scooters/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooter endpoint should return forbidden, if auth has only 'scooter:view' scope and scooter is not free")
    @WithMockUserId(scopes = "SCOPE_scooter:view")
    public void getScooterInfoShouldReturnForbiddenWhenScooterViewAndScooterNotFree() throws Exception {
        ScooterResponse response = new ScooterResponse();
        response.setStatus(ScooterStatus.OCCUPIED);
        when(scooterQueryService.getScooterInfo(1)).thenReturn(response);

        mockMvc.perform(get("/api/scooters/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scooter endpoint should return ok, if auth has 'scooter:view_by_status' scope regardless of scooter status")
    @WithMockUserId(scopes = {"SCOPE_scooter:view_by_status", "SCOPE_scooter:view"})
    public void getScooterInfoShouldReturnOkWhenScooterViewByStatus() throws Exception {
        ScooterResponse response = new ScooterResponse();
        response.setStatus(ScooterStatus.OCCUPIED);
        when(scooterQueryService.getScooterInfo(1)).thenReturn(response);

        mockMvc.perform(get("/api/scooters/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return unauthorized error response, if auth hasn't 'scooter:manage' scope")
    public void postScooterShouldReturnErrorWhenNoScooterManage() throws Exception {
        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCreateRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return created with location, if auth has 'scooter:manage' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void postScooterShouldReturnCreatedWhenScooterManage() throws Exception {
        ScooterResponse response = new ScooterResponse();
        response.setId(1);
        when(scooterService.addScooter(any())).thenReturn(response);

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/scooters/1"));
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return validation exception error response, if serial number is empty")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void postScooterShouldReturnErrorWhenEmptySerialNumber() throws Exception {
        CreateScooterRequest request = buildValidCreateRequest();
        request.setSerialNumber(null);

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("serialNumber")));

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return validation exception error response, if serial number has trailing spaces")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void postScooterShouldReturnErrorWhenTrailingSpacesInSerialNumber() throws Exception {
        CreateScooterRequest request = buildValidCreateRequest();
        request.setSerialNumber("   SC-001");

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("serialNumber")));

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return validation exception error response, if model ID is empty")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void postScooterShouldReturnErrorWhenEmptyModelId() throws Exception {
        CreateScooterRequest request = buildValidCreateRequest();
        request.setModelId(null);

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("modelId")));

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return validation exception error response, if model ID is not positive")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void postScooterShouldReturnErrorWhenModelIdIsNotPositive() throws Exception {
        CreateScooterRequest request = buildValidCreateRequest();
        request.setModelId(0);

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("modelId")));

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return validation exception error response, if battery is empty")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void postScooterShouldReturnErrorWhenEmptyBattery() throws Exception {
        CreateScooterRequest request = buildValidCreateRequest();
        request.setBattery(null);

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("battery")));

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return validation exception error response, if battery is negative")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void postScooterShouldReturnErrorWhenBatteryIsNegative() throws Exception {
        CreateScooterRequest request = buildValidCreateRequest();
        request.setBattery(-1);

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("battery")));

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return validation exception error response, if battery exceeds 100")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void postScooterShouldReturnErrorWhenBatteryExceeds100() throws Exception {
        CreateScooterRequest request = buildValidCreateRequest();
        request.setBattery(101);

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("battery")));

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return validation exception error response, if location is empty")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void postScooterShouldReturnErrorWhenEmptyLocation() throws Exception {
        CreateScooterRequest request = buildValidCreateRequest();
        request.setLocation(null);

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("location")));

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post scooter endpoint should return validation exception error response, if status is empty")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void postScooterShouldReturnErrorWhenEmptyStatus() throws Exception {
        CreateScooterRequest request = buildValidCreateRequest();
        request.setStatus(null);

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("status")));

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch scooter info endpoint should return unauthorized error response, if auth hasn't 'scooter:manage' scope")
    public void patchScooterInfoShouldReturnErrorWhenNoScooterManage() throws Exception {
        mockMvc.perform(patch("/api/scooters/1/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateScooterInfoRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch scooter info endpoint should return ok, if auth has 'scooter:manage' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void patchScooterInfoShouldReturnOkWhenScooterManage() throws Exception {
        mockMvc.perform(patch("/api/scooters/1/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateScooterInfoRequest())))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch scooter info endpoint should return validation exception error response, if model ID is not positive")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void patchScooterInfoShouldReturnErrorWhenModelIdIsNotPositive() throws Exception {
        UpdateScooterInfoRequest request = new UpdateScooterInfoRequest();
        request.setModelId(0);

        mockMvc.perform(patch("/api/scooters/1/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("modelId")));

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete scooter endpoint should return unauthorized error response, if auth hasn't 'scooter:manage' scope")
    public void deleteScooterShouldReturnErrorWhenNoScooterManage() throws Exception {
        mockMvc.perform(delete("/api/scooters/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scooterService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete scooter endpoint should return no content, if auth has 'scooter:manage' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void deleteScooterShouldReturnNoContentWhenScooterManage() throws Exception {
        mockMvc.perform(delete("/api/scooters/1"))
                .andExpect(status().isNoContent());
    }

    private CreateScooterRequest buildValidCreateRequest() {
        GeometryFactory factory = new GeometryFactory();
        CreateScooterRequest request = new CreateScooterRequest();
        request.setSerialNumber("SC-001");
        request.setModelId(1);
        request.setBattery(80);
        request.setLocation(factory.createPoint(new Coordinate(37.0, 55.0)));
        request.setStatus(ScooterStatus.FREE);
        return request;
    }
}
