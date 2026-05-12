package com.chump.user.controller;

import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.test_config.TestConfig;
import com.chump.test_config.WithMockUserId;
import com.chump.user.service.query.ScopeQueryService;
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        ScopeController.class,
        ScopeControllerTest.Config.class,
        JwtAuthenticationEntryPoint.class,
        RestExceptionHandler.class,
        TestConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.expiration-time=100000",
        "auth.jwt.secret-key=SECRETKEYSECRETKEYSECRETKEYSECRETKEY"
})
@WebAppConfiguration
@DisplayName("User controller testing")
public class ScopeControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired ScopeQueryService scopeQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(scopeQueryService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public ScopeQueryService scopeQueryService() {
            return Mockito.mock(ScopeQueryService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scopes endpoint should return unauthorized error response, if auth hasn't 'role:manage' scope")
    public void getScopesShouldReturnErrorWhenNoRoleManage() throws Exception {
        mockMvc.perform(get("/api/scopes"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scopeQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scopes endpoint should return ok, if auth has 'role:manage' scope")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void getScopesShouldReturnErrorWhenRoleManage() throws Exception {
        mockMvc.perform(get("/api/scopes"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scopes endpoint should return validation exception error response, if page is not positive")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void getScopesShouldReturnErrorWhenPageIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/scopes")
                        .param("page",  "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("page")));

        verifyNoInteractions(scopeQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get scopes endpoint should return validation exception error response, if page size is not positive")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void getScopesShouldReturnErrorWhenPageSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/scopes")
                        .param("pageSize",  "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("pageSize")));

        verifyNoInteractions(scopeQueryService);
    }
}
