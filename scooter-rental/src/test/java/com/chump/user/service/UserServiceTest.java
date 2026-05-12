package com.chump.user.service;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.rental.dao.TripDao;
import com.chump.rental.model.Trip;
import com.chump.user.dao.RoleDao;
import com.chump.user.dao.UserDao;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.dto.command.UpdateUserBaseInfoCommand;
import com.chump.user.dto.command.UpdateUserProtectedInfoCommand;
import com.chump.user.mapper.UserMapper;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User query service testing")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserDao userDao;
    @Mock private UserProfileDao userProfileDao;
    @Mock private RoleDao roleDao;
    @Mock private UserMapper mapper;
    @Mock private TripDao tripDao;

    @InjectMocks
    private UserService service;

    @Test
    @Tag("unit")
    @DisplayName("Update user base info method should throw an exception, if user ID is unknown")
    public void updateUserBaseInfoShouldThrowWhenUnknownUserId() {
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.updateUserBaseInfo(1, UpdateUserBaseInfoCommand.builder().build()));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown user ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Update user protected info method should throw an exception, if user ID is unknown")
    public void updateUserProtectedInfoShouldThrowWhenUnknownUserId() {
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.updateUserProtectedInfo(1, UpdateUserProtectedInfoCommand.builder().build()));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown user ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Update user role method should throw an exception, if user ID is unknown")
    public void updateUserRoleShouldThrowWhenUnknownUserId() {
        when(userDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.updateUserRole(1, 1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown user ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Update user role method should throw an exception, if role ID is unknown")
    public void updateUserRoleShouldThrowWhenUnknownRoleId() {
        when(userDao.findById(anyInt())).thenReturn(Optional.of(
                User.builder().build()
        ));
        when(roleDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.updateUserRole(1, 1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown role ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Delete user method should throw an exception, if there're ongoing trips")
    public void deleteUserShouldThrowWhenOngoingTrips() {
        when(tripDao.findOngoingByUserId(anyInt())).thenReturn(Collections.singletonList(
                Trip.builder().build()
        ));

        assertThrows(UnavaliableActionException.class, () -> service.deleteUser(1, false));
    }

    @Test
    @Tag("unit")
    @DisplayName("Delete user method should throw an exception, if user's balance not zero and not forces")
    public void deleteUsersShouldThrowWhenNotZeroBalanceAndNotForced() {
        when(tripDao.findOngoingByUserId(anyInt())).thenReturn(Collections.emptyList());
        when(userProfileDao.findById(anyInt())).thenReturn(Optional.of(new UserProfile(
                1,
                null,
                null,
                null,
                BigDecimal.ONE,
                BigDecimal.ZERO
            )
        ));

        assertThrows(UnavaliableActionException.class, () -> service.deleteUser(1, false));
    }
}
