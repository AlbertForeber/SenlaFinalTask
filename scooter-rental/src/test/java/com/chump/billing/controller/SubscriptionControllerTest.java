package com.chump.billing.controller;

import com.chump.billing.dto.request.CreateSubscriptionTariffRequest;
import com.chump.billing.dto.request.UpdateSubscriptionTariffRequest;
import com.chump.billing.dto.response.SubscriptionTariffResponse;
import com.chump.billing.mapper.SubscriptionMapperImpl;
import com.chump.billing.mapper.TariffMapperImpl;
import com.chump.billing.service.BillingService;
import com.chump.billing.service.SubscriptionService;
import com.chump.billing.service.query.SubscriptionQueryService;
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
        SubscriptionController.class,
        SubscriptionControllerTest.Config.class,
        SubscriptionMapperImpl.class,
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
public class SubscriptionControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired SubscriptionService subscriptionService;
    @Autowired BillingService billingService;
    @Autowired SubscriptionQueryService subscriptionQueryService;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(subscriptionService);
        Mockito.reset(billingService);
        Mockito.reset(subscriptionQueryService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public SubscriptionService subscriptionService() {
            return Mockito.mock(SubscriptionService.class);
        }

        @Bean
        public SubscriptionQueryService subscriptionQueryService() {
            return Mockito.mock(SubscriptionQueryService.class);
        }

        @Bean
        public BillingService billingService() {
            return Mockito.mock(BillingService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Get subscriptions endpoint should return unauthorized error response, if auth hasn't 'tariff:view' scope")
    public void getSubscriptionsShouldReturnErrorWhenNoTariffView() throws Exception {
        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(subscriptionQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get subscriptions endpoint should return ok, if auth has 'tariff:view' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:view")
    public void getSubscriptionsShouldReturnOkWhenTariffView() throws Exception {
        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get subscriptions endpoint should return validation exception error response, if page is not positive")
    @WithMockUserId(scopes = "SCOPE_tariff:view")
    public void getSubscriptionsShouldReturnErrorWhenPageIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/subscriptions")
                        .param("page",  "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("page")));

        verifyNoInteractions(subscriptionQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get subscriptions endpoint should return validation exception error response, if page size is not positive")
    @WithMockUserId(scopes = "SCOPE_tariff:view")
    public void getSubscriptionsShouldReturnErrorWhenPageSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/subscriptions")
                        .param("pageSize",  "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("pageSize")));

        verifyNoInteractions(subscriptionQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get subscription endpoint should return unauthorized error response, if auth hasn't 'tariff:view' scope")
    public void getSubscriptionShouldReturnErrorWhenNoTariffView() throws Exception {
        mockMvc.perform(get("/api/subscriptions/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(subscriptionQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get subscription endpoint should return ok, if auth has 'tariff:view' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:view")
    public void getSubscriptionShouldReturnOkWhenTariffView() throws Exception {
        mockMvc.perform(get("/api/subscriptions/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get my subscription endpoint should return unauthorized error response, if auth hasn't 'tariff:view' scope")
    public void getMySubscriptionShouldReturnErrorWhenNoTariffView() throws Exception {
        mockMvc.perform(get("/api/subscriptions/my"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(subscriptionQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get my subscription endpoint should return ok, if auth has 'tariff:view' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:view")
    public void getMySubscriptionShouldReturnOkWhenTariffView() throws Exception {
        mockMvc.perform(get("/api/subscriptions/my"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user subscription endpoint should return unauthorized error response, if auth hasn't 'tariff:view_admin' scope")
    public void getUserSubscriptionShouldReturnErrorWhenNoTariffViewAdmin() throws Exception {
        mockMvc.perform(get("/api/subscriptions")
                .param("user_id", "1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(subscriptionQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user subscription endpoint should return ok, if auth has 'tariff:view_admin' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:view_admin")
    public void getUserSubscriptionShouldReturnOkWhenTariffViewAdmin() throws Exception {
        mockMvc.perform(get("/api/subscriptions")
                .param("user_id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user subscription endpoint should return validation exception error response, if param 'user_id' is not positive")
    @WithMockUserId(scopes = "SCOPE_tariff:view_admin")
    public void getUserSubscriptionShouldReturnOkWhenNegativeUserId() throws Exception {
        mockMvc.perform(get("/api/subscriptions")
                .param("user_id", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("user_id")));
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscribe endpoint should return unauthorized error response, if auth hasn't 'tariff:subscribe' scope")
    public void postSubscribeShouldReturnErrorWhenNoTariffSubscribe() throws Exception {
        mockMvc.perform(post("/api/subscriptions/1/subscribe"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscribe endpoint should return created, if auth has 'tariff:subscribe' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:subscribe")
    public void postSubscribeShouldReturnCreatedWhenTariffSubscribe() throws Exception {
        mockMvc.perform(post("/api/subscriptions/1/subscribe"))
                .andExpect(status().isCreated());
    }

    @Test
    @Tag("integration")
    @DisplayName("Post unsubscribe endpoint should return unauthorized error response, if auth hasn't 'tariff:subscribe' scope")
    public void postUnsubscribeShouldReturnErrorWhenNoTariffSubscribe() throws Exception {
        mockMvc.perform(post("/api/subscriptions/1/unsubscribe"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete unsubscribe endpoint should return no content, if auth has 'tariff:subscribe' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:subscribe")
    public void deleteUnsubscribeShouldReturnNoContentWhenTariffSubscribe() throws Exception {
        mockMvc.perform(delete("/api/subscriptions/unsubscribe"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return unauthorized error response, if auth hasn't 'tariff:manage' scope")
    public void postSubscriptionShouldReturnErrorWhenNoTariffManage() throws Exception {
        mockMvc.perform(post("/api/subscriptions/1/unsubscribe"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return created with uri, if auth has 'tariff:manage' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postSubscriptionShouldReturnCreatedWhenTariffManage() throws Exception {
        CreateSubscriptionTariffRequest request = new CreateSubscriptionTariffRequest();
        request.setName("test");
        request.setBasePrice(BigDecimal.ONE);
        request.setDurationDays(1);

        SubscriptionTariffResponse response = new SubscriptionTariffResponse();
        response.setId(1);
        when(subscriptionService.addSubscriptionTariff(any())).thenReturn(response);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/subscriptions/1"));
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return validation exception error response, if subscription name is empty")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postSubscriptionShouldReturnErrorWhenEmptyName() throws Exception {
        CreateSubscriptionTariffRequest request = new CreateSubscriptionTariffRequest();
        request.setName(null);
        request.setBasePrice(BigDecimal.ONE);
        request.setDurationDays(1);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return validation exception error response, if subscription name has trailing spaces")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postSubscriptionShouldReturnErrorWhenTrailingSpacesInName() throws Exception {
        CreateSubscriptionTariffRequest request = new CreateSubscriptionTariffRequest();
        request.setName("           test");
        request.setBasePrice(BigDecimal.ONE);
        request.setDurationDays(1);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return validation exception error response, if subscription name's too big")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postSubscriptionShouldReturnErrorWhenNameTooLong() throws Exception {
        CreateSubscriptionTariffRequest request = new CreateSubscriptionTariffRequest();
        request.setName("12345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567" +
                "812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678" +
                "12345678123456781234567812345678123456781234567812345678812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678" +
                "12345678123456781234567812345678123456781234567812345678");
        request.setBasePrice(BigDecimal.ONE);
        request.setDurationDays(1);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return validation exception error response, if base price is empty")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postSubscriptionShouldReturnErrorWhenEmptyBasePrice() throws Exception {
        CreateSubscriptionTariffRequest request = new CreateSubscriptionTariffRequest();
        request.setName("test");
        request.setBasePrice(null);
        request.setDurationDays(1);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("basePrice")));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return validation exception error response, if base price is too specific")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postSubscriptionShouldReturnErrorWhenBasePriceTooSpecific() throws Exception {
        CreateSubscriptionTariffRequest request = new CreateSubscriptionTariffRequest();
        request.setName("test");
        request.setBasePrice(BigDecimal.valueOf(100001, 8));
        request.setDurationDays(1);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("basePrice")));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return validation exception error response, if base price is too big")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postSubscriptionShouldReturnErrorWhenBasePriceTooLong() throws Exception {
        CreateSubscriptionTariffRequest request = new CreateSubscriptionTariffRequest();
        request.setName("test");
        request.setBasePrice(BigDecimal.valueOf(100001));
        request.setDurationDays(1);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("basePrice")));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return validation exception error response, if duration days is empty")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postSubscriptionShouldReturnErrorWhenEmptyDurationDays() throws Exception {
        CreateSubscriptionTariffRequest request = new CreateSubscriptionTariffRequest();
        request.setName("test");
        request.setBasePrice(BigDecimal.ONE);
        request.setDurationDays(null);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("durationDays")));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return validation exception error response, if duration days is not positive")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void postSubscriptionShouldReturnErrorWhenNotPositiveDurationDays() throws Exception {
        CreateSubscriptionTariffRequest request = new CreateSubscriptionTariffRequest();
        request.setName("test");
        request.setBasePrice(BigDecimal.ONE);
        request.setDurationDays(0);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("durationDays")));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch subscription endpoint should return unauthorized error response, if auth hasn't 'tariff:manage' scope")
    public void patchSubscriptionShouldReturnErrorWhenNoTariffManage() throws Exception {
        mockMvc.perform(patch("/api/subscriptions/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(subscriptionService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return ok with uri, if auth has 'tariff:manage' scope")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void patchSubscriptionShouldReturnCreatedWhenTariffManage() throws Exception {
        UpdateSubscriptionTariffRequest request = new UpdateSubscriptionTariffRequest();
        request.setDurationDays(1);

        mockMvc.perform(patch("/api/subscriptions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Post subscription endpoint should return validation exception error response, if duration days is not positive")
    @WithMockUserId(scopes = "SCOPE_tariff:manage")
    public void patchSubscriptionShouldReturnErrorWhenNotPositiveDurationDays() throws Exception {
        UpdateSubscriptionTariffRequest request = new UpdateSubscriptionTariffRequest();
        request.setDurationDays(0);

        mockMvc.perform(patch("/api/subscriptions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("durationDays")));

        verifyNoInteractions(subscriptionService);
    }
}
