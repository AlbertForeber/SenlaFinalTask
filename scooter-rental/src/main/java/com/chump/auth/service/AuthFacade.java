package com.chump.auth.service;

import com.chump.auth.dto.command.LoginCommand;
import com.chump.auth.dto.command.RegisterCommand;
import com.chump.auth.dto.response.TokenResponse;
import com.chump.auth.mapper.AuthMapper;
import com.chump.auth.model.Session;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.user.dao.RoleDao;
import com.chump.user.dao.UserDao;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.model.Role;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthenticationManager authenticationManager;
    private final SessionService sessionService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RoleDao roleDao;
    private final UserDao userDao;
    private final UserProfileDao userProfileDao;
    private final AuthMapper authMapper;

    @Transactional
    public TokenResponse login(LoginCommand command) {
        User user = (User) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        command.getUsername(),
                        command.getPassword()
                )).getPrincipal();

        String accessToken = jwtService.generateToken(user);
        String refreshToken = sessionService.generateSession(
                user,
                command.getDeviceName(),
                command.getIpAddress()
        ).getRefreshToken();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenResponse register(RegisterCommand command) {
        if (userDao.existsByUsername(command.getUsername())) {
            throw new UnavaliableActionException("User with such username already exists");
        }

        User user = createUser(command);
        UserProfile userProfile = authMapper.toUserProfileEntity(command, user);
        userProfileDao.save(userProfile);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = sessionService.generateSession(
                user,
                command.getDeviceName(),
                command.getIpAddress()
        ).getRefreshToken();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void logout(int userId, String refreshToken) {
        sessionService.terminateSessionByToken(userId, refreshToken);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        Session newSession = sessionService.rotateSessionRefreshToken(refreshToken);
        String newAccessToken = jwtService.generateToken(newSession.getUser());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newSession.getRefreshToken())
                .build();
    }

    private User createUser(RegisterCommand command) {
        Role defaultRole = roleDao.getDefaultRole().orElseThrow(
                () -> new NoSuchEntityException("No default role found. Contact support service")
        );

        User user = authMapper.toUserEntity(command, defaultRole);
        user.setPassword(passwordEncoder.encode(command.getPassword()));

        return userDao.save(user);
    }
}
