package com.chump.rental.controller;

import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.rental.dto.request.CreateRentalSpotRequest;
import com.chump.rental.dto.request.UpdateRentalSpotRequest;
import com.chump.rental.dto.response.RentalSpotDetailedResponse;
import com.chump.rental.mapper.RentalSpotMapperImpl;
import com.chump.rental.mapper.ScooterMapperImpl;
import com.chump.rental.mapper.ScooterModelMapperImpl;
import com.chump.rental.service.RentalSpotService;
import com.chump.rental.service.query.RentalSpotQueryService;
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
import org.locationtech.jts.geom.Polygon;
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
        RentalSpotController.class,
        RentalSpotControllerTest.Config.class,
        RentalSpotMapperImpl.class,
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
@DisplayName("Rental spot controller testing")
public class RentalSpotControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired RentalSpotQueryService rentalSpotQueryService;
    @Autowired RentalSpotService rentalSpotService;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(rentalSpotQueryService);
        Mockito.reset(rentalSpotService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public RentalSpotQueryService rentalSpotQueryService() {
            return Mockito.mock(RentalSpotQueryService.class);
        }

        @Bean
        public RentalSpotService rentalSpotService() {
            return Mockito.mock(RentalSpotService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Get rental spots endpoint should return unauthorized error response, if auth hasn't 'spot:view' scope")
    public void getRentalSpotsShouldReturnErrorWhenNoSpotView() throws Exception {
        mockMvc.perform(get("/api/rental-spots"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalSpotQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get rental spots endpoint should return ok, if auth has 'spot:view' scope")
    @WithMockUserId(scopes = "SCOPE_spot:view")
    public void getRentalSpotsShouldReturnOkWhenSpotView() throws Exception {
        mockMvc.perform(get("/api/rental-spots"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get rental spot endpoint should return unauthorized error response, if auth hasn't 'spot:view' scope")
    public void getRentalSpotShouldReturnErrorWhenNoSpotView() throws Exception {
        mockMvc.perform(get("/api/rental-spots/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalSpotQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get rental spot endpoint should return ok, if auth has 'spot:view' scope")
    @WithMockUserId(scopes = "SCOPE_spot:view")
    public void getRentalSpotShouldReturnOkWhenSpotView() throws Exception {
        mockMvc.perform(get("/api/rental-spots/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get rental spot scooters endpoint should return unauthorized error response, if auth hasn't 'spot:view' scope")
    public void getRentalSpotScootersShouldReturnErrorWhenNoSpotView() throws Exception {
        mockMvc.perform(get("/api/rental-spots/1/scooters"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalSpotQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get rental spot scooters endpoint should return ok, if auth has 'spot:view' scope")
    @WithMockUserId(scopes = "SCOPE_spot:view")
    public void getRentalSpotScootersShouldReturnOkWhenSpotView() throws Exception {
        mockMvc.perform(get("/api/rental-spots/1/scooters"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get rental spot detailed info endpoint should return unauthorized error response, if auth hasn't 'spot:view_admin' scope")
    public void getRentalSpotDetailedInfoShouldReturnErrorWhenNoSpotViewAdmin() throws Exception {
        mockMvc.perform(get("/api/rental-spots/1/info"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalSpotQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get rental spot detailed info endpoint should return ok, if auth has 'spot:view_admin' scope")
    @WithMockUserId(scopes = "SCOPE_spot:view_admin")
    public void getRentalSpotDetailedInfoShouldReturnOkWhenSpotViewAdmin() throws Exception {
        mockMvc.perform(get("/api/rental-spots/1/info"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get nearby rental spots endpoint should return unauthorized error response, if auth hasn't 'spot:view' scope")
    public void getNearbyRentalSpotsShouldReturnErrorWhenNoSpotView() throws Exception {
        mockMvc.perform(get("/api/rental-spots")
                        .param("latitude", "55.0")
                        .param("longitude", "37.0")
                        .param("radius", "500"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalSpotQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get nearby rental spots endpoint should return ok, if auth has 'spot:view' scope")
    @WithMockUserId(scopes = "SCOPE_spot:view")
    public void getNearbyRentalSpotsShouldReturnOkWhenSpotView() throws Exception {
        mockMvc.perform(get("/api/rental-spots")
                        .param("latitude", "55.0")
                        .param("longitude", "37.0")
                        .param("radius", "500"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get nearby rental spots endpoint should return validation exception error response, if latitude is out of range")
    @WithMockUserId(scopes = "SCOPE_spot:view")
    public void getNearbyRentalSpotsShouldReturnErrorWhenLatitudeOutOfRange() throws Exception {
        mockMvc.perform(get("/api/rental-spots")
                        .param("latitude", "91.0")
                        .param("longitude", "37.0")
                        .param("radius", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("latitude")));

        verifyNoInteractions(rentalSpotQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get nearby rental spots endpoint should return validation exception error response, if longitude is out of range")
    @WithMockUserId(scopes = "SCOPE_spot:view")
    public void getNearbyRentalSpotsShouldReturnErrorWhenLongitudeOutOfRange() throws Exception {
        mockMvc.perform(get("/api/rental-spots")
                        .param("latitude", "55.0")
                        .param("longitude", "181.0")
                        .param("radius", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("longitude")));

        verifyNoInteractions(rentalSpotQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get nearby rental spots endpoint should return validation exception error response, if radius is not positive")
    @WithMockUserId(scopes = "SCOPE_spot:view")
    public void getNearbyRentalSpotsShouldReturnErrorWhenRadiusIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/rental-spots")
                        .param("latitude", "55.0")
                        .param("longitude", "37.0")
                        .param("radius", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("radius")));

        verifyNoInteractions(rentalSpotQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post rental spot endpoint should return unauthorized error response, if auth hasn't 'spot:manage' scope")
    public void postRentalSpotShouldReturnErrorWhenNoSpotManage() throws Exception {
        CreateRentalSpotRequest request = new CreateRentalSpotRequest();
        request.setName("Test spot");
        request.setArea(buildTestPolygon());
        request.setIsParking(false);

        mockMvc.perform(post("/api/rental-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalSpotService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post rental spot endpoint should return created with location, if auth has 'spot:manage' scope")
    @WithMockUserId(scopes = "SCOPE_spot:manage")
    public void postRentalSpotShouldReturnCreatedWhenSpotManage() throws Exception {
        CreateRentalSpotRequest request = new CreateRentalSpotRequest();
        request.setName("Test spot");
        request.setArea(buildTestPolygon());
        request.setIsParking(false);

        RentalSpotDetailedResponse response = new RentalSpotDetailedResponse();
        response.setId(1);
        when(rentalSpotService.openSpot(any())).thenReturn(response);

        mockMvc.perform(post("/api/rental-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/rental-spots/1"));
    }

    @Test
    @Tag("integration")
    @DisplayName("Post rental spot endpoint should return validation exception error response, if name is empty")
    @WithMockUserId(scopes = "SCOPE_spot:manage")
    public void postRentalSpotShouldReturnErrorWhenEmptyName() throws Exception {
        CreateRentalSpotRequest request = new CreateRentalSpotRequest();
        request.setName(null);
        request.setArea(buildTestPolygon());
        request.setIsParking(false);

        mockMvc.perform(post("/api/rental-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(rentalSpotService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post rental spot endpoint should return validation exception error response, if name has trailing spaces")
    @WithMockUserId(scopes = "SCOPE_spot:manage")
    public void postRentalSpotShouldReturnErrorWhenTrailingSpacesInName() throws Exception {
        CreateRentalSpotRequest request = new CreateRentalSpotRequest();
        request.setName("   Test spot");
        request.setArea(buildTestPolygon());
        request.setIsParking(false);

        mockMvc.perform(post("/api/rental-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(rentalSpotService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post rental spot endpoint should return validation exception error response, if name is too long")
    @WithMockUserId(scopes = "SCOPE_spot:manage")
    public void postRentalSpotShouldReturnErrorWhenNameTooLong() throws Exception {
        CreateRentalSpotRequest request = new CreateRentalSpotRequest();
        request.setName("A".repeat(101));
        request.setArea(buildTestPolygon());
        request.setIsParking(false);

        mockMvc.perform(post("/api/rental-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(rentalSpotService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post rental spot endpoint should return validation exception error response, if area is empty")
    @WithMockUserId(scopes = "SCOPE_spot:manage")
    public void postRentalSpotShouldReturnErrorWhenEmptyArea() throws Exception {
        CreateRentalSpotRequest request = new CreateRentalSpotRequest();
        request.setName("Test spot");
        request.setArea(null);
        request.setIsParking(false);

        mockMvc.perform(post("/api/rental-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("area")));

        verifyNoInteractions(rentalSpotService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post rental spot endpoint should return validation exception error response, if isParking is empty")
    @WithMockUserId(scopes = "SCOPE_spot:manage")
    public void postRentalSpotShouldReturnErrorWhenEmptyIsParking() throws Exception {
        CreateRentalSpotRequest request = new CreateRentalSpotRequest();
        request.setName("Test spot");
        request.setArea(buildTestPolygon());
        request.setIsParking(null);

        mockMvc.perform(post("/api/rental-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("isParking")));

        verifyNoInteractions(rentalSpotService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch rental spot endpoint should return unauthorized error response, if auth hasn't 'spot:manage' scope")
    public void patchRentalSpotShouldReturnErrorWhenNoSpotManage() throws Exception {
        mockMvc.perform(patch("/api/rental-spots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRentalSpotRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalSpotService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch rental spot endpoint should return ok, if auth has 'spot:manage' scope")
    @WithMockUserId(scopes = "SCOPE_spot:manage")
    public void patchRentalSpotShouldReturnOkWhenSpotManage() throws Exception {
        mockMvc.perform(patch("/api/rental-spots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRentalSpotRequest())))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch rental spot endpoint should return validation exception error response, if name is too long")
    @WithMockUserId(scopes = "SCOPE_spot:manage")
    public void patchRentalSpotShouldReturnErrorWhenNameTooLong() throws Exception {
        UpdateRentalSpotRequest request = new UpdateRentalSpotRequest();
        request.setName("A".repeat(101));

        mockMvc.perform(patch("/api/rental-spots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(rentalSpotService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete rental spot endpoint should return unauthorized error response, if auth hasn't 'spot:manage' scope")
    public void deleteRentalSpotShouldReturnErrorWhenNoSpotManage() throws Exception {
        mockMvc.perform(delete("/api/rental-spots/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalSpotService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete rental spot endpoint should return no content, if auth has 'spot:manage' scope")
    @WithMockUserId(scopes = "SCOPE_spot:manage")
    public void deleteRentalSpotShouldReturnNoContentWhenSpotManage() throws Exception {
        mockMvc.perform(delete("/api/rental-spots/1"))
                .andExpect(status().isNoContent());
    }

    private Polygon buildTestPolygon() {
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coords = {
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1),
                new Coordinate(0, 0)
        };
        return factory.createPolygon(coords);
    }
}
