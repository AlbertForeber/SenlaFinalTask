package com.chump.user.controller;


import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.rental.service.query.TripQueryService;
import com.chump.test_config.TestConfig;
import com.chump.test_config.WithMockUserId;
import com.chump.user.dto.request.UpdateUserProtectedInfoRequest;
import com.chump.user.dto.request.UpdateUserRoleRequest;
import com.chump.user.mapper.RoleMapperImpl;
import com.chump.user.mapper.ScopeMapperImpl;
import com.chump.user.mapper.UserMapperImpl;
import com.chump.user.service.UserService;
import com.chump.user.service.query.UserQueryService;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.math.BigDecimal;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        UserAdminController.class,
        UserAdminControllerTest.Config.class,
        UserMapperImpl.class,
        RoleMapperImpl.class,
        ScopeMapperImpl.class,
        JwtAuthenticationEntryPoint.class,
        RestExceptionHandler.class,
        TestConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.expiration-time=100000",
        "auth.jwt.secret-key=SECRETKEYSECRETKEYSECRETKEYSECRETKEY"
})
@WebAppConfiguration
@DisplayName("User admin controller testing")
public class UserAdminControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired UserService userService;
    @Autowired UserQueryService userQueryService;
    @Autowired TripQueryService tripQueryService;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(userService, userQueryService, tripQueryService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public UserQueryService userQueryService() {
            return Mockito.mock(UserQueryService.class);
        }

        @Bean
        public TripQueryService tripQueryService() {
            return Mockito.mock(TripQueryService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user trip history endpoint should return unauthorized error response, if auth hasn't 'profile:view_admin' scope")
    public void getUserTripHistoryShouldReturnErrorWhenNoProfileViewAdmin() throws Exception {
        mockMvc.perform(get("/api/user/1/history"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tripQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user trip history endpoint should return ok, if auth has 'profile:view_admin' scope")
    @WithMockUserId(scopes = "SCOPE_profile:view_admin")
    public void getUserTripHistoryShouldReturnOkWhenProfileViewAdmin() throws Exception {
        mockMvc.perform(get("/api/user/1/history"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user trip history endpoint should return validation exception error response, if page is not positive")
    @WithMockUserId(scopes = "SCOPE_profile:view_admin")
    public void getUserTripHistoryShouldReturnErrorWhenPageIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/user/1/history")
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("page")));

        verifyNoInteractions(tripQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user trip history endpoint should return validation exception error response, if page size is not positive")
    @WithMockUserId(scopes = "SCOPE_profile:view_admin")
    public void getUserTripHistoryShouldReturnErrorWhenPageSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/user/1/history")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("pageSize")));

        verifyNoInteractions(tripQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user profile endpoint should return unauthorized error response, if auth hasn't 'profile:view_admin' scope")
    public void getUserProfileShouldReturnErrorWhenNoProfileViewAdmin() throws Exception {
        mockMvc.perform(get("/api/user/1/profile"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get user profile endpoint should return ok, if auth has 'profile:view_admin' scope")
    @WithMockUserId(scopes = "SCOPE_profile:view_admin")
    public void getUserProfileShouldReturnOkWhenProfileViewAdmin() throws Exception {
        mockMvc.perform(get("/api/user/1/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch user profile endpoint should return unauthorized error response, if auth hasn't 'profile:manage_admin' scope")
    public void patchUserProfileShouldReturnErrorWhenNoProfileManageAdmin() throws Exception {
        mockMvc.perform(patch("/api/user/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserProtectedInfoRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch user profile endpoint should return ok, if auth has 'profile:manage_admin' scope")
    @WithMockUserId(scopes = "SCOPE_profile:manage_admin")
    public void patchUserProfileShouldReturnOkWhenProfileManageAdmin() throws Exception {
        mockMvc.perform(patch("/api/user/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserProtectedInfoRequest())))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch user profile endpoint should return validation exception error response, if balance is negative")
    @WithMockUserId(scopes = "SCOPE_profile:manage_admin")
    public void patchUserProfileShouldReturnErrorWhenBalanceIsNegative() throws Exception {
        UpdateUserProtectedInfoRequest request = new UpdateUserProtectedInfoRequest();
        request.setBalance(BigDecimal.valueOf(-1));

        mockMvc.perform(patch("/api/user/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("balance")));

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch user profile endpoint should return validation exception error response, if balance is too precise")
    @WithMockUserId(scopes = "SCOPE_profile:manage_admin")
    public void patchUserProfileShouldReturnErrorWhenBalanceIsTooPrecise() throws Exception {
        UpdateUserProtectedInfoRequest request = new UpdateUserProtectedInfoRequest();
        request.setBalance(BigDecimal.valueOf(100000001, 8));

        mockMvc.perform(patch("/api/user/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("balance")));

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch user profile endpoint should return validation exception error response, if discount is negative")
    @WithMockUserId(scopes = "SCOPE_profile:manage_admin")
    public void patchUserProfileShouldReturnErrorWhenDiscountIsNegative() throws Exception {
        UpdateUserProtectedInfoRequest request = new UpdateUserProtectedInfoRequest();
        request.setDiscount(BigDecimal.valueOf(-1));

        mockMvc.perform(patch("/api/user/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("discount")));

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch user profile endpoint should return validation exception error response, if discount exceeds 1")
    @WithMockUserId(scopes = "SCOPE_profile:manage_admin")
    public void patchUserProfileShouldReturnErrorWhenDiscountExceedsMax() throws Exception {
        UpdateUserProtectedInfoRequest request = new UpdateUserProtectedInfoRequest();
        request.setDiscount(BigDecimal.valueOf(2));

        mockMvc.perform(patch("/api/user/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("discount")));

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete user profile endpoint should return unauthorized error response, if auth hasn't 'profile:manage_admin' scope")
    public void deleteUserProfileShouldReturnErrorWhenNoProfileManageAdmin() throws Exception {
        mockMvc.perform(delete("/api/user/1/profile"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete user profile endpoint should return no content, if auth has 'profile:manage_admin' scope")
    @WithMockUserId(scopes = "SCOPE_profile:manage_admin")
    public void deleteUserProfileShouldReturnNoContentWhenProfileManageAdmin() throws Exception {
        mockMvc.perform(delete("/api/user/1/profile"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch user role endpoint should return unauthorized error response, if auth hasn't 'profile:manage_role' scope")
    public void patchUserRoleShouldReturnErrorWhenNoProfileManageRole() throws Exception {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setNewRoleId(1);

        mockMvc.perform(patch("/api/user/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch user role endpoint should return ok, if auth has 'profile:manage_role' scope")
    @WithMockUserId(scopes = "SCOPE_profile:manage_role")
    public void patchUserRoleShouldReturnOkWhenProfileManageRole() throws Exception {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setNewRoleId(1);

        mockMvc.perform(patch("/api/user/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch user role endpoint should return validation exception error response, if new role ID is empty")
    @WithMockUserId(scopes = "SCOPE_profile:manage_role")
    public void patchUserRoleShouldReturnErrorWhenEmptyNewRoleId() throws Exception {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setNewRoleId(null);

        mockMvc.perform(patch("/api/user/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("newRoleId")));

        verifyNoInteractions(userService);
    }
}