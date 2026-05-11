package com.chump.billing.controller;

import com.chump.billing.service.BillingService;
import com.chump.billing.service.query.BillingFailureQueryService;
import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.rental.dto.request.ManualBillingRequest;
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        BillingController.class,
        BillingControllerTest.Config.class,
        JwtAuthenticationEntryPoint.class,
        RestExceptionHandler.class,
        TestConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.expiration-time=100000",
        "auth.jwt.secret-key=SECRETKEYSECRETKEYSECRETKEYSECRETKEY"
})
@WebAppConfiguration
@DisplayName("Billing controller testing")
public class BillingControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired BillingFailureQueryService billingFailureQueryService;
    @Autowired BillingService billingService;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(billingFailureQueryService);
        Mockito.reset(billingService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public BillingFailureQueryService billingFailureQueryService() {
            return Mockito.mock(BillingFailureQueryService.class);
        }

        @Bean
        public BillingService billingService() {
            return Mockito.mock(BillingService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Get failures endpoint should return unauthorized error response, if auth hasn't 'billing:view_admin' scope")
    public void getFailuresShouldReturnErrorWhenNoBillingViewAdmin() throws Exception {
        mockMvc.perform(get("/api/billing/failures"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(billingFailureQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get failures endpoint should return ok, if auth has 'billing:view_admin' scope")
    @WithMockUserId(scopes = "SCOPE_billing:view_admin")
    public void getFailuresShouldReturnErrorWhenBillingViewAdmin() throws Exception {
        mockMvc.perform(get("/api/billing/failures"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get failures endpoint should return validation exception error response, if page is not positive")
    @WithMockUserId(scopes = "SCOPE_billing:view_admin")
    public void getFailuresShouldReturnErrorWhenPageIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/billing/failures")
                        .param("page",  "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("page")));

        verifyNoInteractions(billingFailureQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get failures endpoint should return validation exception error response, if page size is not positive")
    @WithMockUserId(scopes = "SCOPE_billing:view_admin")
    public void getFailuresShouldReturnErrorWhenPageSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/billing/failures")
                        .param("pageSize",  "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("pageSize")));

        verifyNoInteractions(billingFailureQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post manual batching endpoint should return unauthorized error response, if auth hasn't 'billing:manage_admin' scope")
    public void postManualBatchingShouldReturnErrorWhenNoBillingManageAdmin() throws Exception {
        ManualBillingRequest request = new ManualBillingRequest();

        mockMvc.perform(post("/api/billing/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(billingFailureQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post manual batching endpoint should return ok, if auth has 'billing:manage_admin' scope")
    @WithMockUserId(scopes = "SCOPE_billing:manage_admin")
    public void postManualBatchingShouldReturnErrorWhenBillingManageAdmin() throws Exception {
        ManualBillingRequest request = new ManualBillingRequest();

        mockMvc.perform(post("/api/billing/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
