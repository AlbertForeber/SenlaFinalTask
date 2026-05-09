package com.chump.user.service.query;

import com.chump.user.dao.ScopeDao;
import com.chump.user.dto.response.ScopeResponse;
import com.chump.user.mapper.ScopeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScopeQueryService {

    private final ScopeDao scopeDao;
    private final ScopeMapper scopeMapper;

    public ScopeQueryService(ScopeDao scopeDao, ScopeMapper scopeMapper) {
        this.scopeDao = scopeDao;
        this.scopeMapper = scopeMapper;
    }

    @Transactional(readOnly = true)
    public List<ScopeResponse> getAllScopes(int pageSize, int page) {
        return scopeMapper.toResponseList(scopeDao.batchFindAll(pageSize, page - 1));
    }
}
