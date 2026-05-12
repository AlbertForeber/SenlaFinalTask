package com.chump.billing.controller;

import com.chump.billing.dto.request.CreateTariffRequest;
import com.chump.billing.dto.request.UpdateTariffRequest;
import com.chump.billing.dto.response.TariffDetailedResponse;
import com.chump.billing.mapper.TariffMapperImpl;
import com.chump.billing.service.TariffService;
import com.chump.billing.service.query.TariffQueryService;
import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.test_config.TestConfig;
import com.chump.test_config.WithMockUserId;
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

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        TariffController.class,
        TariffControllerTest.Config.class,
        TariffMapperImpl.class,
        JwtAuthenticationEntryPoint.class,
        RestExceptionHandler.class,
        TestConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.expiration-time=100000",
        "auth.jwt.secret-key=SECRETKEYSECRETKEYSECRETKEYSECRETKEY"
})
@WebAppConfiguration
@DisplayName("Subscription controller testing")
public class TariffControllerTest {

    @Autowired
    WebApplicationContext context;
    @Autowired
    TariffQueryService tariffQueryService;
    @Autowired
    TariffService tariffService;
    @Autowired
    ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(tariffQueryService);
        Mockito.reset(tariffService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public TariffQueryService tariffQueryService() {
            return Mockito.mock(TariffQueryService.class);
        }

        @Bean
        public TariffService tariffService() {
            return Mockito.mock(TariffService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Get tariffs endpoint should return unauthorized error response, if auth hasn't 'tariff:view' scope")
    public void getTariffsShouldReturnErrorWhenNoTariffView() throws Exception {
        mockMvc.perform(get("/api/tariffs"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tariffQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get tariffs endpoint should return ok, if auth has 'tariff:view' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:view")
    public void getTariffsShouldReturnErrorWhenTariffView() throws Exception {
        mockMvc.perform(get("/api/tariffs"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get tariffs endpoint should return validation exception error response, if page is not positive")
    @WithMockUserId(scopes = "SCOPE_tariff:view")
    public void getTariffsShouldReturnErrorWhenPageIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/tariffs")
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("page")));

        verifyNoInteractions(tariffQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get tariffs endpoint should return validation exception error response, if page size is not positive")
    @WithMockUserId(scopes = "SCOPE_tariff:view")
    public void getTariffsShouldReturnErrorWhenPageSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/tariffs")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("pageSize")));

        verifyNoInteractions(tariffQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get tariff endpoint should return unauthorized error response, if auth hasn't 'tariff:view' scope")
    public void getTariffShouldReturnErrorWhenNoTariffView() throws Exception {
        mockMvc.perform(get("/api/tariffs/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tariffQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get tariff endpoint should return ok, if auth has 'tariff:view' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:view")
    public void getTariffShouldReturnErrorWhenTariffView() throws Exception {
        mockMvc.perform(get("/api/tariffs/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch tariff endpoint should return unauthorized error response, if auth hasn't 'tariff:manage' scope")
    public void patchTariffShouldReturnErrorWhenNoTariffManage() throws Exception {
        UpdateTariffRequest request = new UpdateTariffRequest();

        mockMvc.perform(patch("/api/tariffs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tariffService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch tariff endpoint should return ok, if auth has 'tariff:manage' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void patchTariffShouldReturnErrorWhenTariffManage() throws Exception {
        UpdateTariffRequest request = new UpdateTariffRequest();

        mockMvc.perform(patch("/api/tariffs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch tariff endpoint should return validation exception error response, if tariff name is too big")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void patchTariffShouldReturnErrorWhenNameIsTooLong() throws Exception {
        UpdateTariffRequest request = new UpdateTariffRequest();
        request.setName("12345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567" +
                "812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678" +
                "12345678123456781234567812345678123456781234567812345678812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678" +
                "12345678123456781234567812345678123456781234567812345678");

        mockMvc.perform(patch("/api/tariffs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(tariffService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch tariff endpoint should return validation exception error response, if base price is too precise")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void patchTariffShouldReturnErrorWhenBasePriceIsTooPrecise() throws Exception {
        UpdateTariffRequest request = new UpdateTariffRequest();
        request.setBasePrice(BigDecimal.valueOf(10000001, 8));

        mockMvc.perform(patch("/api/tariffs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("basePrice")));

        verifyNoInteractions(tariffService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch tariff endpoint should return validation exception error response, if base price is too big")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void patchTariffShouldReturnErrorWhenBasePriceIsTooLong() throws Exception {
        UpdateTariffRequest request = new UpdateTariffRequest();
        request.setBasePrice(BigDecimal.valueOf(10000001));

        mockMvc.perform(patch("/api/tariffs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("basePrice")));

        verifyNoInteractions(tariffService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch tariff endpoint should return validation exception error response, if interval is not positive")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void patchTariffShouldReturnErrorWhenIntervalIsNotPositive() throws Exception {
        UpdateTariffRequest request = new UpdateTariffRequest();
        request.setInterval(0);

        mockMvc.perform(patch("/api/tariffs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("interval")));

        verifyNoInteractions(tariffService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post tariff endpoint should return unauthorized error response, if auth hasn't 'tariff:manage' scope")
    public void postTariffShouldReturnErrorWhenNoTariffManage() throws Exception {
        CreateTariffRequest request = new CreateTariffRequest();
        request.setName("test");
        request.setInterval(1);
        request.setBasePrice(BigDecimal.ZERO);

        mockMvc.perform(post("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tariffService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post tariff endpoint should return created, if auth has 'tariff:manage' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postTariffShouldReturnErrorWhenTariffManage() throws Exception {
        CreateTariffRequest request = new CreateTariffRequest();
        request.setName("test");
        request.setInterval(1);
        request.setBasePrice(BigDecimal.ZERO);

        TariffDetailedResponse response = new TariffDetailedResponse();
        response.setId(1);
        response.setName("test");
        response.setBasePrice(BigDecimal.ZERO);
        response.setBillingIntervalMinutes("1");

        when(tariffService.addTariff(any())).thenReturn(response);

        mockMvc.perform(post("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tariffs/1"));
    }

    @Test
    @Tag("integration")
    @DisplayName("Post tariff endpoint should return created, if name is empty")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postTariffShouldReturnErrorWhenEmptyName() throws Exception {
        CreateTariffRequest request = new CreateTariffRequest();
        request.setName(null);
        request.setInterval(1);
        request.setBasePrice(BigDecimal.ZERO);

        mockMvc.perform(post("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));
    }

    @Test
    @Tag("integration")
    @DisplayName("Post tariff endpoint should return created, if base price is empty")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postTariffShouldReturnErrorWhenEmptyBasePrice() throws Exception {
        CreateTariffRequest request = new CreateTariffRequest();
        request.setName("test");
        request.setInterval(1);
        request.setBasePrice(null);

        mockMvc.perform(post("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("basePrice")));
    }

    @Test
    @Tag("integration")
    @DisplayName("Post tariff endpoint should return created, if interval is empty")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postTariffShouldReturnErrorWhenEmptyInterval() throws Exception {
        CreateTariffRequest request = new CreateTariffRequest();
        request.setName("test");
        request.setInterval(null);
        request.setBasePrice(BigDecimal.ZERO);

        mockMvc.perform(post("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("interval")));
    }

    @Test
    @Tag("integration")
    @DisplayName("Post tariff endpoint should return validation exception error response, if tariff name is too big")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postTariffShouldReturnErrorWhenNameIsTooLong() throws Exception {
        CreateTariffRequest request = new CreateTariffRequest();
        request.setInterval(1);
        request.setBasePrice(BigDecimal.ZERO);
        request.setName("12345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567" +
                "812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678" +
                "12345678123456781234567812345678123456781234567812345678812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678" +
                "12345678123456781234567812345678123456781234567812345678");

        mockMvc.perform(post("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(tariffService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post tariff endpoint should return validation exception error response, if base price is too precise")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postTariffShouldReturnErrorWhenBasePriceIsTooPrecise() throws Exception {
        CreateTariffRequest request = new CreateTariffRequest();
        request.setName("test");
        request.setInterval(1);
        request.setBasePrice(BigDecimal.valueOf(10000001, 8));

        mockMvc.perform(post("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("basePrice")));

        verifyNoInteractions(tariffService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post tariff endpoint should return validation exception error response, if base price is too big")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postTariffShouldReturnErrorWhenBasePriceIsTooLong() throws Exception {
        CreateTariffRequest request = new CreateTariffRequest();
        request.setBasePrice(BigDecimal.valueOf(10000001));
        request.setName("test");
        request.setInterval(1);

        mockMvc.perform(post("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("basePrice")));

        verifyNoInteractions(tariffService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post tariff endpoint should return validation exception error response, if interval is not positive")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postTariffShouldReturnErrorWhenIntervalIsNotPositive() throws Exception {
        CreateTariffRequest request = new CreateTariffRequest();
        request.setInterval(0);
        request.setName("test");
        request.setBasePrice(BigDecimal.ZERO);

        mockMvc.perform(post("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("interval")));

        verifyNoInteractions(tariffService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete tariff endpoint should return ok, if auth has 'tariff:manage' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void deleteTariffShouldReturnErrorWhenTariffManage() throws Exception {
        mockMvc.perform(delete("/api/tariffs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete tariff endpoint should return unauthorized error response, if auth hasn't 'tariff:manage' scope")
    public void deleteTariffShouldReturnErrorWhenNoTariffManage() throws Exception {
        mockMvc.perform(delete("/api/tariffs/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tariffService);
    }
}