package com.chump.user.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.common.utils.TransactionUtils;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final UserProfileDao userProfileDao;
    private final RoleDao roleDao;
    private final UserMapper mapper;
    private final TripDao tripDao;
    private final TransactionUtils transactionUtils;

    @Transactional
    public UserProfileResponse updateUserBaseInfo(int userId, UpdateUserBaseInfoCommand command) {
        UserProfile result = userProfileDao.findById(userId).orElseThrow(
                () -> new NoSuchEntityException("No user found with id: " + userId)
        );

        mapper.updateUserBaseInfoFromCommand(command, result);
        transactionUtils.afterCommit(() ->
                log.info("Successfully updated user profile base info for user with id: {}", userId)
        );

        return mapper.toUserProfileResponse(result);
    }

    @Transactional
    public UserProfileResponse updateUserProtectedInfo(int userId, UpdateUserProtectedInfoCommand command) {
        UserProfile result = userProfileDao.findById(userId).orElseThrow(
                () -> new NoSuchEntityException("No user found with id: " + userId)
        );

        mapper.updateUserProtectedInfoFromCommand(command, result);
        transactionUtils.afterCommit(() ->
                log.info("Successfully updated user profile protected info for user with id: {}", userId)
        );

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
        transactionUtils.afterCommit(() ->
                log.info("Successfully updated role for user with id: {}. New role id: {}", userId, roleId)
        );

        return mapper.toUserRoleResponse(user);
    }

    @Transactional
    public void deleteUser(int userId, boolean isForce) {
        if (!tripDao.findOngoingByUserId(userId).isEmpty()) {
            throw new UnavaliableActionException("Forbidden to delete user with ongoing trips");
        }

        userProfileDao.findById(userId).ifPresent(userProfile -> {
            if (!isForce && userProfile.getBalance().longValue() > 0) {
                throw new UnavaliableActionException("Forbidden to delete user with not zero balance. " +
                        "Use 'force=true' to force delete");
            }
        });

        log.info("Successfully deleted user with id: {}", userId);
        userDao.delete(userId);
    }
}