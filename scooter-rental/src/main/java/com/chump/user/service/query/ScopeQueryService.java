package com.chump.user.service.query;

import com.chump.common.utils.TransactionUtils;
import com.chump.user.dao.ScopeDao;
import com.chump.user.dto.response.ScopeResponse;
import com.chump.user.mapper.ScopeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScopeQueryService {

    private final ScopeDao scopeDao;
    private final ScopeMapper scopeMapper;
    private final TransactionUtils transactionUtils;

    @Transactional(readOnly = true)
    public List<ScopeResponse> getAllScopes(int pageSize, int page) {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got all scopes")
        );

        return scopeMapper.toResponseList(scopeDao.batchFindAll(pageSize, page - 1));
    }
}
