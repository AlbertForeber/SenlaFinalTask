package com.chump.auth.controller;

import com.chump.auth.dto.request.LoginRequest;
import com.chump.auth.dto.request.LogoutRequest;
import com.chump.auth.dto.request.RefreshRequest;
import com.chump.auth.dto.request.RegisterRequest;
import com.chump.auth.mapper.AuthMapperImpl;
import com.chump.auth.service.AuthFacade;
import com.chump.common.exception.advice.RestExceptionHandler;
import com.chump.common.security.JwtAuthenticationEntryPoint;
import com.chump.common.security.SecurityConfig;
import com.chump.common.utils.DeviceInfoResolver;
import com.chump.common.config.TestConfig;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ua_parser.Parser;


import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SecurityConfig.class,
        AuthController.class,
        AuthControllerTest.Config.class,
        AuthMapperImpl.class,
        DeviceInfoResolver.class,
        Parser.class,
        JwtAuthenticationEntryPoint.class,
        RestExceptionHandler.class,
        TestConfig.class
})
@TestPropertySource(properties = {
        "auth.jwt.expiration-time=100000",
        "auth.jwt.secret-key=SECRETKEYSECRETKEYSECRETKEYSECRETKEY"
})
@WebAppConfiguration
@DisplayName("Auth controller testing")
public class AuthControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired AuthFacade authFacade;
    @Autowired ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        Mockito.reset(authFacade);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class Config {

        @Bean
        public AuthFacade authFacade() {
            return Mockito.mock(AuthFacade.class);
        }
    }

    @Test
    @Tag("integration")
    @DisplayName("Login endpoint should return validation exception error response, if username is empty")
    public void loginShouldReturnErrorWhenEmptyUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setPassword("testtest");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("username")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Login endpoint should return validation exception error response, if username has trailing spaces")
    public void loginShouldReturnErrorWhenTrailingSpacesInUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("    test");
        request.setPassword("testtest");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("username")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Login endpoint should return validation exception error response, if password is empty")
    public void loginShouldReturnErrorWhenEmptyPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("password")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Login endpoint should return bad credentials error response, if credentials are invalid")
    public void loginShouldReturnErrorWhenBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test");
        request.setPassword("testtest");
        when(authFacade.login(any())).thenThrow(new BadCredentialsException("No user found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Bad credentials"));

    }

    @Test
    @Tag("integration")
    @DisplayName("Login endpoint resolve IP and device name correctly")
    public void loginShouldResolveIpAndDeviceNameCorrectly() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test");
        request.setPassword("testtest");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
                        .remoteAddress("192.168.1.4")
                )
                .andExpect(status().isOk());

        verify(authFacade).login(argThat(
                cmd -> cmd.getDeviceName().equals("K (Android)") &&
                        cmd.getIpAddress().equals("192.168.1.4"))
        );
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if username's empty")
    public void registerShouldReturnErrorWhenEmptyUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("testtest");
        request.setEmail("test@example.com");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("username")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if username's trailing spaces")
    public void registerShouldReturnErrorWhenUsernameHasTrailingSpaces() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("       test");
        request.setPassword("testtest");
        request.setEmail("test@example.com");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("username")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if username's too small")
    public void registerShouldReturnErrorWhenUsernameTooSmall() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("te");
        request.setPassword("testtest");
        request.setEmail("test@example.com");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("username")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if username's too long")
    public void registerShouldReturnErrorWhenUsernameTooLong() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testtesttesttesttesttest");
        request.setPassword("testtest");
        request.setEmail("test@example.com");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("username")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if password's empty")
    public void registerShouldReturnErrorWhenEmptyPassword() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("test");
        request.setEmail("test@example.com");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("password")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if password's too small")
    public void registerShouldReturnErrorWhenPasswordTooSmall() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("test");
        request.setPassword("test");
        request.setEmail("test@example.com");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("password")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if date of birth's empty")
    public void registerShouldReturnErrorWhenEmptyDateOfBirth() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("test");
        request.setPassword("testtest");
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("dateOfBirth")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if date of birth's not in the past")
    public void registerShouldReturnErrorWhenDateOfBirthNotPast() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("test");
        request.setPassword("testtest");
        request.setEmail("test@example.com");
        request.setDateOfBirth(LocalDate.now());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("dateOfBirth")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if email's empty")
    public void registerShouldReturnErrorWhenEmptyEmail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("test");
        request.setPassword("testtest");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("email")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if email's too small")
    public void registerShouldReturnErrorWhenEmailTooSmall() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("test");
        request.setPassword("testtest");
        request.setEmail("t@e.r");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("email")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if email's too long")
    public void registerShouldReturnErrorWhenEmailTooLong() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("test");
        request.setPassword("testtest");
        request.setEmail("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890@example.com");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("email")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Register endpoint should return validation exception error response, if email's malformed")
    public void registerShouldReturnErrorWhenEmailMalformed() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("test");
        request.setPassword("testtest");
        request.setEmail("test");
        request.setDateOfBirth(LocalDate.now().minusYears(18));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("email")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Refresh endpoint should return validation exception error response, if old refresh token's empty")
    public void refreshShouldReturnErrorWhenEmptyOldRefreshToken() throws Exception {
        RefreshRequest request = new RefreshRequest();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("oldRefreshToken")));

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Logout endpoint should return unauthorized error response, if logout's empty")
    public void logoutShouldReturnErrorWhenNoUser() throws Exception {
        LogoutRequest request = new LogoutRequest();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authFacade);
    }

    @Test
    @Tag("integration")
    @DisplayName("Logout endpoint should return validation exception error response, if logout's empty")
    @WithMockUser
    public void logoutShouldReturnErrorWhenEmptyRefreshToken() throws Exception {
        LogoutRequest request = new LogoutRequest();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation exception"))
                .andExpect(jsonPath("$.details[0]").value(containsString("refreshToken")));

        verifyNoInteractions(authFacade);
    }
}