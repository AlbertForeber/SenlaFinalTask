package com.chump.user.service.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.user.dao.UserDao;
import com.chump.user.dao.UserProfileDao;
import com.chump.user.dto.response.UserProfileResponse;
import com.chump.user.mapper.UserMapper;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserProfileDao dao;
    private final UserMapper mapper;
    private final UserDao userDao;

    @Transactional(readOnly = true) // 14.2.2
    public UserProfileResponse getUserProfile(int userId) {
        UserProfile result = dao.findByIdWithUser(userId).orElseThrow(
                () -> new NoSuchEntityException("No user found with id: " + userId)
        );

        return mapper.toUserProfileResponse(result);
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userDao.findByUsernameWithScopes(username).orElseThrow(
                () -> new NoSuchEntityException("No user found with username: " + username)
        );
    }
}
