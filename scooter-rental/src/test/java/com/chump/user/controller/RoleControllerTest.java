package com.chump.user.controller;

import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.test_config.TestConfig;
import com.chump.test_config.WithMockUserId;
import com.chump.user.dto.request.CreateRoleRequest;
import com.chump.user.dto.request.UpdateRoleRequest;
import com.chump.user.dto.response.RoleWithScopesResponse;
import com.chump.user.mapper.RoleMapperImpl;
import com.chump.user.mapper.ScopeMapperImpl;
import com.chump.user.service.RoleService;
import com.chump.user.service.query.RoleQueryService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        RoleController.class,
        RoleControllerTest.Config.class,
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
@DisplayName("Role controller testing")
public class RoleControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired RoleQueryService roleQueryService;
    @Autowired RoleService roleService;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(roleQueryService, roleService);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public RoleQueryService roleQueryService() {
            return Mockito.mock(RoleQueryService.class);
        }

        @Bean
        public RoleService roleService() {
            return Mockito.mock(RoleService.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Get roles endpoint should return unauthorized error response, if auth hasn't 'role:view' scope")
    public void getRolesShouldReturnErrorWhenNoRoleView() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(roleQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get roles endpoint should return ok, if auth has 'role:view' scope")
    @WithMockUserId(scopes = "SCOPE_role:view")
    public void getRolesShouldReturnOkWhenRoleView() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Get role endpoint should return unauthorized error response, if auth hasn't 'role:view' scope")
    public void getRoleWithScopesShouldReturnErrorWhenNoRoleView() throws Exception {
        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(roleQueryService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Get role endpoint should return ok, if auth has 'role:view' scope")
    @WithMockUserId(scopes = "SCOPE_role:view")
    public void getRoleWithScopesShouldReturnOkWhenRoleView() throws Exception {
        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Post role endpoint should return unauthorized error response, if auth hasn't 'role:manage' scope")
    public void postRoleShouldReturnErrorWhenNoRoleManage() throws Exception {
        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCreateRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(roleService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post role endpoint should return created with location, if auth has 'role:manage' scope")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void postRoleShouldReturnCreatedWhenRoleManage() throws Exception {
        RoleWithScopesResponse response = new RoleWithScopesResponse();
        response.setId(1);
        when(roleService.addRole(any())).thenReturn(response);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/roles/1"));
    }

    @Test
    @Tag("integration")
    @DisplayName("Post role endpoint should return validation exception error response, if name is empty")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void postRoleShouldReturnErrorWhenEmptyName() throws Exception {
        CreateRoleRequest request = buildValidCreateRequest();
        request.setName(null);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(roleService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post role endpoint should return validation exception error response, if name has trailing spaces")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void postRoleShouldReturnErrorWhenTrailingSpacesInName() throws Exception {
        CreateRoleRequest request = buildValidCreateRequest();
        request.setName("   ADMIN");

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(roleService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post role endpoint should return validation exception error response, if scope IDs list is empty")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void postRoleShouldReturnErrorWhenEmptyScopeIds() throws Exception {
        CreateRoleRequest request = buildValidCreateRequest();
        request.setScopeIds(Collections.emptyList());

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("scopeIds")));

        verifyNoInteractions(roleService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Post role endpoint should return validation exception error response, if scope IDs list is null")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void postRoleShouldReturnErrorWhenNullScopeIds() throws Exception {
        CreateRoleRequest request = buildValidCreateRequest();
        request.setScopeIds(null);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("scopeIds")));

        verifyNoInteractions(roleService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch role endpoint should return unauthorized error response, if auth hasn't 'role:manage' scope")
    public void patchRoleShouldReturnErrorWhenNoRoleManage() throws Exception {
        mockMvc.perform(patch("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRoleRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(roleService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch role endpoint should return ok, if auth has 'role:manage' scope")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void patchRoleShouldReturnOkWhenRoleManage() throws Exception {
        mockMvc.perform(patch("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRoleRequest())))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("integration")
    @DisplayName("Patch role endpoint should return validation exception error response, if name has trailing spaces")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void patchRoleShouldReturnErrorWhenTrailingSpacesInName() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("   ADMIN");

        mockMvc.perform(patch("/api/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("name")));

        verifyNoInteractions(roleService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete role endpoint should return unauthorized error response, if auth hasn't 'role:manage' scope")
    public void deleteRoleShouldReturnErrorWhenNoRoleManage() throws Exception {
        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(roleService);
    }

    @Test
    @Tag("integration")
    @DisplayName("Delete role endpoint should return no content, if auth has 'role:manage' scope")
    @WithMockUserId(scopes = "SCOPE_role:manage")
    public void deleteRoleShouldReturnNoContentWhenRoleManage() throws Exception {
        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isNoContent());
    }

    private CreateRoleRequest buildValidCreateRequest() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("ADMIN");
        request.setScopeIds(List.of(1, 2, 3));
        return request;
    }
}