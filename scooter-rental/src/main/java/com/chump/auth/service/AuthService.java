package com.chump.auth.service;

import com.chump.auth.dto.command.LoginCommand;
import com.chump.auth.dto.command.RegisterCommand;
import com.chump.auth.dto.response.TokenResponse;
import com.chump.auth.mapper.AuthMapper;
import com.chump.auth.model.RefreshToken;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableAction;
import com.chump.user.dao.RoleDao;
import com.chump.user.dao.UserDao;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.model.Role;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RoleDao roleDao;
    private final UserDao userDao;
    private final UserProfileDao userProfileDao;
    private final AuthMapper authMapper;

    private static Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       PasswordEncoder passwordEncoder,
                       RoleDao roleDao,
                       UserDao userDao,
                       UserProfileDao userProfileDao,
                       AuthMapper authMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.roleDao = roleDao;
        this.userDao = userDao;
        this.userProfileDao = userProfileDao;
        this.authMapper = authMapper;
    }

    @Transactional
    public TokenResponse login(LoginCommand command) {
        User user = (User) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        command.getUsername(),
                        command.getPassword()
                )).getPrincipal();

        // Подчищаем старые refresh-токены при многократном входе
        // для устранения риска компроментации неиспользованных токенов
        // TODO добавить привязку к устройству для входа с нескольких устройств
        refreshTokenService.revokeUserTokens(user.getId());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.generateToken(user).getToken();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenResponse register(RegisterCommand command) {
        if (userDao.existsByUsername(command.getUsername())) {
            throw new UnavaliableAction("User with such username already exists");
        }

        User user = createUser(command);
        UserProfile userProfile = authMapper.toUserProfileEntity(command, user);
        userProfileDao.save(userProfile);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.generateToken(user).getToken();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void logout(int userId) {
        refreshTokenService.deleteUserTokens(userId);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        RefreshToken newRefreshToken = refreshTokenService.rotateToken(refreshToken);
        String newAccessToken = jwtService.generateToken(newRefreshToken.getUser());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    private User createUser(RegisterCommand command) {
        Role defaultRole = roleDao.getDefaultRole().orElseThrow(
                () -> new NoSuchEntityException("No default role found. Contact support service.")
        );

        User user = authMapper.toUserEntity(command, defaultRole);
        user.setPassword(passwordEncoder.encode(command.getPassword()));

        return userDao.save(user);
    }
}
