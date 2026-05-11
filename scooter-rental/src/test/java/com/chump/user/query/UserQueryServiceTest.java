package com.chump.user.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.user.dao.UserDao;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.mapper.UserMapper;
import com.chump.user.service.query.UserQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User query service testing")
@ExtendWith(MockitoExtension.class)
public class UserQueryServiceTest {

    @Mock private UserProfileDao userProfileDao;
    @Mock private UserMapper userMapper;
    @Mock private UserDao userDao;

    @InjectMocks
    private UserQueryService service;

    @Test
    @Tag("unit")
    @DisplayName("Get user profile method should throw an exception, if user profile ID is unknown")
    public void getUserProfileShouldThrowWhenUnknownRoleId() {
        when(userProfileDao.findByIdWithUser(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.getUserProfile(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown user profile ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Get user by username method should throw an exception, if username is unknown")
    public void getUserByUsernameShouldThrowWhenUnknownUsername() {
        when(userDao.findByUsernameWithScopes(anyString())).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> service.getUserByUsername("test"));
        assertTrue(exception.getMessage().contains("test"),
                "Exception message should contain unknown username");
    }
}
