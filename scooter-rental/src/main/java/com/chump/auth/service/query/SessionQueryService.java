package com.chump.auth.service.query;

import com.chump.auth.dao.SessionDao;
import com.chump.auth.dto.response.SessionResponse;
import com.chump.auth.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionQueryService {

    private final SessionDao sessionDao;
    private final AuthMapper authMapper;

    @Transactional(readOnly = true)
    public List<SessionResponse> getAllUserSessions(int userId) {
        return authMapper.toSessionResponseList(sessionDao.findAllByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getAllSessions(int pageSize, int page) {
        return authMapper.toSessionResponseList(sessionDao.batchFindAll(pageSize, page - 1));
    }
}
