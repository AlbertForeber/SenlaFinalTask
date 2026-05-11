package com.chump.user.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.user.dao.RoleDao;
import com.chump.user.service.query.RoleQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role query service testing")
@ExtendWith(MockitoExtension.class)
public class RoleQueryServiceTest {

    @Mock private RoleDao roleDao;

    @InjectMocks
    private RoleQueryService service;

    @Test
    @Tag("unit")
    @DisplayName("Get role info method should throw an exception, if role ID is unknown")
    public void getRoleInfoShouldThrowWhenUnknownRoleId() {
        when(roleDao.findByIdWithScopes(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.getRoleInfo(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown role ID");
    }
}
