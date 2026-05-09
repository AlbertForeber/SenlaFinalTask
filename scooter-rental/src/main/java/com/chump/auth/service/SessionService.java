package com.chump.auth.service;

import com.chump.auth.dao.SessionDao;
import com.chump.auth.model.Session;
import com.chump.common.exception.AuthException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.notification.service.EmailService;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionDao sessionDao;
    private final UserProfileDao userProfileDao;
    private final EmailService emailService;

    @Value("${auth.refresh-token.expiration-time:604800000}")
    private long expirationTime;

    @Transactional
    public Session generateSession(User user, String deviceName, String ipAddress) {
        String refreshToken = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Session session = Session.builder()
                .refreshToken(refreshToken)
                .user(user)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .expiresAt(now.plusMillis(expirationTime))
                .createdAt(now)
                .build();

        return sessionDao.save(session);
    }

    @Transactional(noRollbackFor = AuthException.class)
    public Session rotateSessionRefreshToken(String token) {
        Session oldToken = sessionDao.findByToken(token).orElseThrow(
                () -> new NoSuchEntityException("No session found with refresh token: " + token.substring(0, 7) + "...")
        );

        if (oldToken.isNotValid()) {
            if (oldToken.getReplacedByToken() != null) {
                handleSessionRefreshTokenReuse(oldToken);
            }
            throw new AuthException("Invalid refresh token");
        }

        Session updatedSession = generateSession(
                oldToken.getUser(),
                oldToken.getDeviceName(),
                oldToken.getIpAddress()
        );
        oldToken.setReplacedByToken(updatedSession);
        oldToken.setTerminated(true);

        return updatedSession;
    }

    @Transactional
    public void terminateSessionByToken(int userId, String token) {
        Session session = sessionDao.findByToken(token).orElseThrow(
                () -> new NoSuchEntityException("No refresh token found: " + token.substring(0, 7) + "...")
        );

        if (session.getUser().getId() != userId || session.isNotValid()) {
            throw new AuthException("Invalid token");
        }

        session.setTerminated(true);
    }

    @Transactional
    public void cleanUpStaleSessions() {
        try {
            int cleaned = sessionDao.deleteStale();
            log.info("Successfully cleaned {} stale sessions", cleaned);
        } catch (Exception e) {
            log.error("Failed to clean up stale sessions", e);
        }
    }

    @Transactional
    public void terminateSession(int sessionId, int userId) {
        Session session = sessionDao.findById(sessionId).orElseThrow(
                () -> new NoSuchEntityException("No session found with id" + sessionId)
        );

        if (session.getUser().getId() != userId) {
            throw new UnavaliableActionException("Forbidden to terminate other user session");
        }

        session.setTerminated(true);
    }

    @Transactional
    public void terminateAllUserSessions(int userId) {
        sessionDao.terminateByUserId(userId);
    }

    private void handleSessionRefreshTokenReuse(Session session) {
        sessionDao.terminateChain(session.getRefreshToken());

        int userId = session.getUser().getId();
        UserProfile userProfile = userProfileDao.findById(userId).orElse(null);
        if (userProfile == null) {
            log.error("No user profile found to notify compromise for user with id: {}", userId);
            return; // Пропуск - ошибка не для пользователя
        }

        emailService.asyncSideSendMail(
                userProfile.getEmail(),
                "Your account might be in danger",
                "It looks like someone else tried to access your account. " +
                        "Please assure that your network is secured"
        );
    }
}
