package com.chump.user.service.query;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.user.dao.RoleDao;
import com.chump.user.dto.response.RoleResponse;
import com.chump.user.mapper.RoleMapper;
import com.chump.user.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleQueryService {

    private final RoleDao roleDao;
    private final RoleMapper roleMapper;

    public RoleQueryService(RoleDao roleDao, RoleMapper roleMapper) {
        this.roleDao = roleDao;
        this.roleMapper = roleMapper;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleMapper.toResponseList(roleDao.findAll());
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleInfo(int roleId) {
        Role result = roleDao.findById(roleId).orElseThrow(
                () -> new NoSuchEntityException("No role found with id: " + roleId)
        );

        return roleMapper.toResponse(result);
    }
}
