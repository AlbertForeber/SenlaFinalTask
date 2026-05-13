package com.chump.user.service.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.utils.TransactionUtils;
import com.chump.user.dao.RoleDao;
import com.chump.user.dto.response.RoleResponse;
import com.chump.user.dto.response.RoleWithScopesResponse;
import com.chump.user.mapper.RoleMapper;
import com.chump.user.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleQueryService {

    private final RoleDao roleDao;
    private final RoleMapper roleMapper;
    private final TransactionUtils transactionUtils;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        transactionUtils.afterCommit(() ->
                log.info("Successfully got all roles")
        );

        return roleMapper.toResponseList(roleDao.findAll());
    }

    @Transactional(readOnly = true)
    public RoleWithScopesResponse getRoleInfo(int roleId) {
        Role result = roleDao.findByIdWithScopes(roleId).orElseThrow(
                () -> new NoSuchEntityException("No role found with id: " + roleId)
        );

        transactionUtils.afterCommit(() ->
                log.info("Successfully got role info for role with id: {}", roleId)
        );

        return roleMapper.toResponseWithScopes(result);
    }
}
