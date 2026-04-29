package com.chump.user.service.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.user.dao.RoleDao;
import com.chump.user.dto.response.RoleResponse;
import com.chump.user.dto.response.RoleWithScopesResponse;
import com.chump.user.mapper.RoleMapper;
import com.chump.user.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleQueryService {

    private final RoleDao roleDao;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleMapper.toResponseList(roleDao.findAll());
    }

    @Transactional(readOnly = true)
    public RoleWithScopesResponse getRoleInfo(int roleId) {
        Role result = roleDao.findByIdWithScopes(roleId).orElseThrow(
                () -> new NoSuchEntityException("No role found with id: " + roleId)
        );

        return roleMapper.toResponseWithScopes(result);
    }
}
