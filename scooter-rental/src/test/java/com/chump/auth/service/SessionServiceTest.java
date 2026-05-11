package com.chump.auth.service;

import com.chump.auth.dao.SessionDao;
import com.chump.auth.model.Session;
import com.chump.auth.service.SessionService;
import com.chump.common.exception.AuthException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.notification.service.EmailService;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Session service testing")
public class SessionServiceTest {

    @Mock private SessionDao sessionDao;
    @Mock private UserProfileDao userProfileDao;
    @Mock private EmailService emailService;

    @InjectMocks
    private SessionService sessionService;
    private Session session;

    @BeforeEach
    public void init() {
        ReflectionTestUtils.setField(sessionService, "expirationTime", 604800000);
        Instant now = Instant.now();

        session = Session.builder()
                .refreshToken("refreshToken")
                .user(User.builder()
                        .id(1)
                        .build())
                .deviceName("deviceName")
                .ipAddress("ipAddress")
                .expiresAt(now.plusMillis(604800000))
                .createdAt(now)
                .build();
    }

    @Test
    @Tag("unit")
    @DisplayName("Generate session method should return valid session")
    public void generateSessionShouldReturnSession() {
        when(sessionDao.save(any())).thenAnswer(returnsFirstArg());
        Session generatedSession = sessionService.generateSession(
                session.getUser(),
                "device_test",
                "ip_test"
        );

        assertAll("Session validation",
                () -> assertEquals(1, generatedSession.getUser().getId(),
                        "Session's user must have given user's ID"),
                () -> assertEquals("device_test", generatedSession.getDeviceName(),
                        "Session's device name must equal given device name"),
                () -> assertEquals("ip_test", generatedSession.getIpAddress(),
                        "Session's ip must equal given ip")
        );
    }

    @Test
    @Tag("unit")
    @DisplayName("Rotate token method should return new session, if token is valid")
    public void rotateTokenShouldReturnNewSessionWhenValid() {
        when(sessionDao.save(any())).thenReturn(session);
        when(sessionDao.findByToken(any())).thenReturn(Optional.of(session));
        Session rotatedSession = sessionService.rotateSessionRefreshToken("test_token");

        assertAll("Old session validation",
                () -> assertNotNull(session.getReplacedByToken(),
                        "Old session should have replace by token field filled"),
                () -> assertTrue(session.getTerminated(),
                        "Old session should be terminated")
        );

        assertAll("New session validation",
                () -> assertEquals(session.getUser().getId(), rotatedSession.getUser().getId(),
                        "Rotated session's user must have old sessions's user ID"),
                () -> assertEquals(session.getDeviceName(), rotatedSession.getDeviceName(),
                        "Rotated session's device name must equal old session's device name"),
                () -> assertEquals(session.getIpAddress(), rotatedSession.getIpAddress(),
                        "Rotated session's ip must equal old session's ip")
        );
    }

    @Test
    @Tag("unit")
    @DisplayName("Rotate token method should throw an exception, if token is unknown")
    public void rotateTokenShouldThrowWhenUnknown() {
        when(sessionDao.findByToken(any())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> sessionService.rotateSessionRefreshToken("testtkn"));

        assertTrue(exception.getMessage().contains("testtk"),
                "Exception message should contain part of invalid token");
    }

    @Test
    @Tag("unit")
    @DisplayName("Rotate token method should throw an exception, if token is expired")
    public void rotateTokenShouldThrowWhenExpired() {
        session.setExpiresAt(session.getCreatedAt().minusMillis(1000));
        when(sessionDao.findByToken(any())).thenReturn(Optional.of(session));

        assertThrows(AuthException.class,
                () -> sessionService.rotateSessionRefreshToken("test_token"));
    }

    @Test
    @Tag("unit")
    @DisplayName("Rotate token method should throw an exception, if token is used")
    public void rotateTokenShouldThrowWhenUsed() {
        session.setReplacedByToken(Session.builder().build());
        when(sessionDao.findByToken(any())).thenReturn(Optional.of(session));
        when(userProfileDao.findById(any())).thenReturn(Optional.of(new UserProfile(
                1,
                null,
                "test@example.com",
                null,
                null,
                null

        )));

        assertThrows(AuthException.class,
                () -> sessionService.rotateSessionRefreshToken("test_token"));

        verify(sessionDao, times(1)).terminateChain(session.getRefreshToken());
        verify(emailService, times(1)).asyncSideSendMail(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    @Tag("unit")
    @DisplayName("Terminate by token method should terminate session, if token is valid")
    public void terminateByTokenShouldTerminateSessionWhenValid() {
        when(sessionDao.findByToken(any())).thenReturn(Optional.of(session));

        sessionService.terminateSessionByToken(1, "test_token");
        assertTrue(session.getTerminated());
    }

    @Test
    @Tag("unit")
    @DisplayName("Terminate by token method should throw, if token is unknown")
    public void terminateByTokenShouldThrowSessionWhenUnknown() {
        when(sessionDao.findByToken(any())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> sessionService.terminateSessionByToken(1, "testtkn"));

        assertTrue(exception.getMessage().contains("testtk"),
                "Exception message should contain part of invalid token");
    }

    @Test
    @Tag("unit")
    @DisplayName("Terminate by token method should throw, if user ID is invalid")
    public void terminateByTokenShouldThrowSessionWhenInvalidUserId() {
        when(sessionDao.findByToken(any())).thenReturn(Optional.of(session));

       assertThrows(AuthException.class,
                () -> sessionService.terminateSessionByToken(2, "test_token"));
    }

    @Test
    @Tag("unit")
    @DisplayName("Terminate by token method should throw, if token is invalid")
    public void terminateByTokenShouldThrowSessionWhenInvalidToken() {
        when(sessionDao.findByToken(any())).thenReturn(Optional.of(session));
        session.setExpiresAt(session.getCreatedAt().minusMillis(1000));

        assertThrows(AuthException.class,
                () -> sessionService.terminateSessionByToken(1, "test_token"));
    }

    @Test
    @Tag("unit")
    @DisplayName("Terminate method should terminate session, if valid")
    public void terminateShouldTerminateSessionWhenValid() {
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));

        sessionService.terminateSession(1, 1);
        assertTrue(session.getTerminated());
    }

    @Test
    @Tag("unit")
    @DisplayName("Terminate method should throw, if session is unknown")
    public void terminateShouldThrowSessionWhenUnknown() {
        when(sessionDao.findById(any())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> sessionService.terminateSession(1, 1));

        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown session ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Terminate method should throw, if user ID is not valid")
    public void terminateShouldThrowWhenInvalidUserId() {
        when(sessionDao.findById(any())).thenReturn(Optional.of(session));

        assertThrows(UnavaliableActionException.class,
                () -> sessionService.terminateSession(1, 2));
    }
}