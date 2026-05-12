package com.chump.user.controller;

import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.rental.service.query.TripQueryService;
import com.chump.test_config.TestConfig;
import com.chump.test_config.WithMockUserId;
import com.chump.user.dto.request.UpdateUserBaseInfoRequest;
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

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        UserController.class,
        UserControllerTest.Config.class,
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
@DisplayName("User controller testing")
public class UserControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired UserService userService;
    @Autowired UserQueryService userQueryService;
    @Autowired TripQueryService tripQueryService;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(userQueryService, tripQueryService, userService);

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
    @DisplayName("Get trip history endpoint should return unauthorized error response, if auth hasn't 'profile:view' scope")
    public void getTripHistoryShouldReturnErrorWhenNoProfileView() throws Exception {
        mockMvc.perform(get("/api/user/history"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tripQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip history endpoint should return ok, if auth has 'profile:view' scope")
    @WithMockUserId(scopes = "SCOPE_profile:view")
    public void getTripHistoryShouldReturnErrorWhenProfileView() throws Exception {
        mockMvc.perform(get("/api/user/history"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip history endpoint should return validation exception error response, if page is not positive")
    @WithMockUserId(scopes = "SCOPE_profile:view")
    public void getTripHistoryShouldReturnErrorWhenPageIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/user/history")
                        .param("page",  "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("page")));

        verifyNoInteractions(userQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get trip history endpoint should return validation exception error response, if page size is not positive")
    @WithMockUserId(scopes = "SCOPE_profile:view")
    public void getTripHistoryShouldReturnErrorWhenPageSizeIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/user/history")
                        .param("pageSize",  "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("pageSize")));

        verifyNoInteractions(userQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get profile endpoint should return unauthorized error response, if auth hasn't 'profile:view' scope")
    public void getProfileShouldReturnErrorWhenNoProfileView() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get profile endpoint should return ok, if auth has 'profile:view' scope")
    @WithMockUserId(scopes = "SCOPE_profile:view")
    public void getProfileShouldReturnErrorWhenProfileView() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch profile endpoint should return unauthorized error response, if auth hasn't 'profile:manage' scope")
    public void patchProfileShouldReturnErrorWhenNoProfileManage() throws Exception {
        mockMvc.perform(patch("/api/user/profile"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch profile endpoint should return ok, if auth has 'profile:manage' scope")
    @WithMockUserId(scopes = "SCOPE_profile:manage")
    public void patchProfileShouldReturnErrorWhenProfileManage() throws Exception {
        UpdateUserBaseInfoRequest request = new UpdateUserBaseInfoRequest();
        request.setEmail("test@example.com");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(patch("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch profile endpoint should return validation exception error response, if email is too short")
    @WithMockUserId(scopes = "SCOPE_profile:manage")
    public void patchProfileShouldReturnErrorWhenEmailIsTooShort() throws Exception {
        UpdateUserBaseInfoRequest request = new UpdateUserBaseInfoRequest();
        request.setEmail("t@r.u");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(patch("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch profile endpoint should return validation exception error response, if email is too long")
    @WithMockUserId(scopes = "SCOPE_profile:manage")
    public void patchProfileShouldReturnErrorWhenEmailIsTooLong() throws Exception {
        UpdateUserBaseInfoRequest request = new UpdateUserBaseInfoRequest();
        request.setEmail("12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "12345678901234567890@example.com");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(patch("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch profile endpoint should return validation exception error response, if date of birth not in the past")
    @WithMockUserId(scopes = "SCOPE_profile:manage")
    public void patchProfileShouldReturnErrorWhenDateOfBirthNotInPast() throws Exception {
        UpdateUserBaseInfoRequest request = new UpdateUserBaseInfoRequest();
        request.setEmail("test@example.com");
        request.setDateOfBirth(LocalDate.now());

        mockMvc.perform(patch("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete profile endpoint should return unauthorized error response, if auth hasn't 'profile:manage' scope")
    public void deleteProfileShouldReturnErrorWhenNoProfileManage() throws Exception {
        mockMvc.perform(delete("/api/user/profile"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete profile endpoint should return no content, if auth has 'profile:manage' scope")
    @WithMockUserId(scopes = "SCOPE_profile:manage")
    public void deleteProfileShouldReturnErrorWhenProfileManage() throws Exception {
        mockMvc.perform(delete("/api/user/profile"))
                .andExpect(status().isNoContent());
    }
}
