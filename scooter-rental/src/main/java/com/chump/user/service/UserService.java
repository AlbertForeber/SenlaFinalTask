package com.chump.user.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableAction;
import com.chump.rental.dao.TripDao;
import com.chump.user.dao.RoleDao;
import com.chump.user.dao.UserDao;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.dto.command.UpdateUserBaseInfoCommand;
import com.chump.user.dto.command.UpdateUserProtectedInfoCommand;
import com.chump.user.dto.response.UserProfileResponse;
import com.chump.user.dto.response.UserRoleResponse;
import com.chump.user.mapper.UserMapper;
import com.chump.user.model.Role;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserDao userDao;
    private final UserProfileDao userProfileDao;
    private final RoleDao roleDao;
    private final UserMapper mapper;
    private final TripDao tripDao;

    public UserService(UserDao userDao,
                       UserProfileDao userProfileDao,
                       RoleDao roleDao,
                       UserMapper mapper,
                       TripDao tripDao) {
        this.userDao = userDao;
        this.userProfileDao = userProfileDao;
        this.roleDao = roleDao;
        this.mapper = mapper;
        this.tripDao = tripDao;
    }

    @Transactional
    public UserProfileResponse updateUserBaseInfo(int userId, UpdateUserBaseInfoCommand command) {
        UserProfile result = userProfileDao.findByIdWithUser(userId).orElseThrow(
                () -> new NoSuchEntityException("No user found with id: " + userId)
        );

        mapper.updateUserBaseInfoFromCommand(command, result);
        return mapper.toUserProfileResponse(result);
    }

    @Transactional
    public UserProfileResponse updateUserProtectedInfo(int userId, UpdateUserProtectedInfoCommand command) {
        UserProfile result = userProfileDao.findByIdWithUser(userId).orElseThrow(
                () -> new NoSuchEntityException("No user found with id: " + userId)
        );

        mapper.updateUserProtectedInfoFromCommand(command, result);
        return mapper.toUserProfileResponse(result);
    }

    @Transactional
    public UserRoleResponse updateUserRole(int userId, int roleId) {
        User user = userDao.findById(userId).orElseThrow(
                () -> new NoSuchEntityException("No user found with id: " + userId)
        );
        Role role = roleDao.findById(roleId).orElseThrow(
                () -> new NoSuchEntityException("No role found with id: " + roleId)
        );

        user.setRole(role);
        return mapper.toUserRoleResponse(user);
    }

    @Transactional
    public void deleteUser(int userId) {
        if (!tripDao.findOngoingByUserId(userId).isEmpty()) {
            throw new UnavaliableAction("Forbidden to delete user with ongoing trips");
        }
        userDao.delete(userId);
    }
}
