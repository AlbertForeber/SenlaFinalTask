package com.chump.auth.service;

import com.chump.auth.dao.RefreshTokenDao;
import com.chump.auth.model.RefreshToken;
import com.chump.common.exception.AuthException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-token.expiration-time:604800000}")
    private long expirationTime;
    private final RefreshTokenDao refreshTokenDao;

    public RefreshTokenService(RefreshTokenDao refreshTokenDao) {
        this.refreshTokenDao = refreshTokenDao;
    }

    @Transactional
    public RefreshToken generateToken(User user) {
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(now.plusMillis(expirationTime))
                .createdAt(now)
                .build();

        return refreshTokenDao.save(refreshToken);
    }

    @Transactional(noRollbackFor = AuthException.class)
    public RefreshToken rotateToken(String token) {
        RefreshToken oldToken = refreshTokenDao.findByToken(token).orElseThrow(
                () -> new NoSuchEntityException("No refresh token found: " + token.substring(0, 7) + "...")
        );

        if (!oldToken.isValid()) {
            if (oldToken.getUsed()) {
                handleReuse(oldToken);
            }
            throw new AuthException("Invalid refresh token");
        }

        RefreshToken newToken = generateToken(oldToken.getUser());
        oldToken.setReplacedByToken(newToken);
        oldToken.setUsed(true);

        return newToken;
    }

    @Transactional
    public void revokeUserTokens(int userId) {
        refreshTokenDao.revokeByUserId(userId);
    }

    @Transactional
    public void deleteUserTokens(int userId) {
        refreshTokenDao.deleteByUserId(userId);
    }

    private void handleReuse(RefreshToken token) {
        // TODO Логирование
        refreshTokenDao.revokeByUserId(token.getUser().getId());
    }
}
