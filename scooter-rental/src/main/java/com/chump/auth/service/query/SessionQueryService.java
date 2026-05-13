package com.chump.auth.service.query;

import com.chump.auth.dao.SessionDao;
import com.chump.auth.dto.response.SessionResponse;
import com.chump.auth.mapper.AuthMapper;
import com.chump.auth.model.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionQueryService {

    private final SessionDao sessionDao;
    private final AuthMapper authMapper;

    @Transactional(readOnly = true)
    public List<SessionResponse> getAllUserActiveSessions(int userId) {
        List<Session> sessions = sessionDao.findAllActiveByUserId(userId);
        log.info("Successfully found sessions for user with id: {}", userId);

        return authMapper.toSessionResponseList(sessions);
    }
}
