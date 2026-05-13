package com.chump.user.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavailableActionException;
import com.chump.user.dao.RoleDao;
import com.chump.user.dao.ScopeDao;
import com.chump.user.dao.UserDao;
import com.chump.user.dto.command.RoleCommand;
import com.chump.user.mapper.RoleMapper;
import com.chump.user.model.Role;
import com.chump.user.model.Scope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role service testing")
@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @Mock private RoleDao roleDao;
    @Mock private ScopeDao scopeDao;
    @Mock private RoleMapper roleMapper;
    @Mock private UserDao userDao;

    @InjectMocks
    private RoleService service;

    @Test
    @Tag("unit")
    @DisplayName("Add role method should throw an exception, if some scopes not found")
    public void addRoleShouldThrowWhenScopesMissing() {
        RoleCommand command = new RoleCommand();
        when(scopeDao.findByIds(List.of(1, 2, 3))).thenReturn(Collections.singletonList(
                new Scope(1, "test")
        ));
        command.setName("test");
        command.setScopeIds(List.of(1, 2, 3));

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.addRole(command));

        assertAll("Exception message must contain not found scope IDs",
                () -> assertTrue(exception.getMessage().contains("2")),
                () -> assertTrue(exception.getMessage().contains("3"))
        );
    }

    @Test
    @Tag("unit")
    @DisplayName("Update role method should throw an exception, if role ID is unknown")
    public void updateRoleShouldThrowWhenUnknownRoleId() {
        when(roleDao.findById(1)).thenReturn(Optional.of(new Role(
                        1,
                        "test",
                        List.of()
                )
        ));

        RoleCommand command = new RoleCommand();
        command.setName("test");
        command.setScopeIds(List.of(1, 2, 3));

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.updateRole(1, command));

        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown role ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Update role method should throw an exception, if some scopes not found")
    public void updateRoleShouldThrowWhenScopesMissing() {
        RoleCommand command = new RoleCommand();
        when(scopeDao.findByIds(List.of(1, 2, 3))).thenReturn(Collections.singletonList(
                new Scope(1, "test")
        ));
        when(roleDao.findById(1)).thenReturn(Optional.of(new Role(
                1,
                "test",
                List.of()
            )
        ));
        command.setName("test");
        command.setScopeIds(List.of(1, 2, 3));

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.updateRole(1, command));

        assertAll("Exception message must contain not found scope IDs",
                () -> assertTrue(exception.getMessage().contains("2")),
                () -> assertTrue(exception.getMessage().contains("3"))
        );
    }

    @Test
    @Tag("unit")
    @DisplayName("Delete role should throw an exception, if there're users with it")
    public void deleteRoleShouldThrowWhenUsersWithIt() {
        when(userDao.findIdsByRoleId(anyInt())).thenReturn(Collections.singletonList(1));

        UnavailableActionException exception = assertThrows(UnavailableActionException.class,
                () -> service.deleteRole(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain IDs of users with this role");
    }
}