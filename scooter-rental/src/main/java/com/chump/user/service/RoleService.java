package com.chump.user.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.user.dao.RoleDao;
import com.chump.user.dao.ScopeDao;
import com.chump.user.dto.command.CreateRoleCommand;
import com.chump.user.dto.response.RoleWithScopesResponse;
import com.chump.user.mapper.RoleMapper;
import com.chump.user.model.Role;
import com.chump.user.model.Scope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleDao roleDao;
    private final ScopeDao scopeDao;
    private final RoleMapper roleMapper;

    @Transactional
    public RoleWithScopesResponse addRole(CreateRoleCommand command) {
        List<Scope> scopes = scopeDao.findByIds(command.getScopeIds());
        if (scopes.size() < command.getScopeIds().size()) {
            throw new NoSuchEntityException("No scopes found with ids: " +
                    command.getScopeIds().removeAll(
                            scopes.stream().map(Scope::getId).toList()
                    )
            );
        }
        Role role = roleDao.save(roleMapper.toEntity(command, scopes));

        return roleMapper.toResponseWithScopes(role);
    }

    @Transactional
    public void deleteRole(int roleId) {
        roleDao.delete(roleId);
    }
}
