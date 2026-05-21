package com.chump.rental.controller;

import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.rental.dto.request.RentScooterRequest;
import com.chump.rental.service.RentalService;
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        ScooterRentController.class,
        ScooterRentControllerTest.Config.class,
        JwtAuthenticationEntryPoint.class,
        RestExceptionHandler.class,
        TestConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.expiration-time=100000",
        "auth.jwt.secret-key=SECRETKEYSECRETKEYSECRETKEYSECRETKEY",
        "rental.minimal-balance=300"
})
@WebAppConfiguration
@DisplayName("Scooter rent controller testing")
public class ScooterRentControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired RentalService rentalService;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(rentalService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public RentalService rentalService() {
            return Mockito.mock(RentalService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Rent scooter endpoint should return unauthorized error response, if auth hasn't 'scooter:rent' scope")
    public void rentScooterShouldReturnErrorWhenNoScooterRent() throws Exception {
        RentScooterRequest request = new RentScooterRequest();
        request.setTariffId(1);

        mockMvc.perform(post("/api/scooters/1/rent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Rent scooter endpoint should return ok, if auth has 'scooter:rent' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:rent")
    public void rentScooterShouldReturnOkWhenScooterRent() throws Exception {
        RentScooterRequest request = new RentScooterRequest();
        request.setTariffId(1);

        mockMvc.perform(post("/api/scooters/1/rent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Rent scooter endpoint should return validation exception error response, if tariff ID is negative")
    @WithMockUserId(scopes = "SCOPE_scooter:rent")
    public void rentScooterShouldReturnErrorWhenNegativeTariffId() throws Exception {
        RentScooterRequest request = new RentScooterRequest();
        request.setTariffId(-1);

        mockMvc.perform(post("/api/scooters/1/rent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("tariffId")));

        verifyNoInteractions(rentalService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Return scooter endpoint should return unauthorized error response, if auth hasn't 'scooter:rent' scope")
    public void returnScooterShouldReturnErrorWhenNoScooterRent() throws Exception {
        mockMvc.perform(post("/api/scooters/1/return"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Return scooter endpoint should return ok, if auth has 'scooter:rent' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:rent")
    public void returnScooterShouldReturnOkWhenScooterRent() throws Exception {
        mockMvc.perform(post("/api/scooters/1/return"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Pause scooter endpoint should return unauthorized error response, if auth hasn't 'scooter:rent' scope")
    public void pauseScooterShouldReturnErrorWhenNoScooterRent() throws Exception {
        mockMvc.perform(post("/api/scooters/1/pause"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Pause scooter endpoint should return ok, if auth has 'scooter:rent' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:rent")
    public void pauseScooterShouldReturnOkWhenScooterRent() throws Exception {
        mockMvc.perform(post("/api/scooters/1/pause"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Resume scooter endpoint should return unauthorized error response, if auth hasn't 'scooter:rent' scope")
    public void resumeScooterShouldReturnErrorWhenNoScooterRent() throws Exception {
        mockMvc.perform(post("/api/scooters/1/resume"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Resume scooter endpoint should return ok, if auth has 'scooter:rent' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:rent")
    public void resumeScooterShouldReturnOkWhenScooterRent() throws Exception {
        mockMvc.perform(post("/api/scooters/1/resume"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Force stop scooter endpoint should return unauthorized error response, if auth hasn't 'scooter:manage' scope")
    public void forceStopScooterShouldReturnErrorWhenNoScooterManage() throws Exception {
        mockMvc.perform(post("/api/scooters/1/force-stop"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(rentalService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Force stop scooter endpoint should return ok, if auth has 'scooter:manage' scope")
    @WithMockUserId(scopes = "SCOPE_scooter:manage")
    public void forceStopScooterShouldReturnOkWhenScooterManage() throws Exception {
        mockMvc.perform(post("/api/scooters/1/force-stop"))
                .andExpect(status().isOk());
    }
}
