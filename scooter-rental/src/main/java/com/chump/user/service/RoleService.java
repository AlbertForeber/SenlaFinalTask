package com.chump.user.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavailableActionException;
import com.chump.common.utils.TransactionUtils;
import com.chump.user.dao.RoleDao;
import com.chump.user.dao.ScopeDao;
import com.chump.user.dao.UserDao;
import com.chump.user.dto.command.RoleCommand;
import com.chump.user.dto.response.RoleWithScopesResponse;
import com.chump.user.mapper.RoleMapper;
import com.chump.user.model.Role;
import com.chump.user.model.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleDao roleDao;
    private final ScopeDao scopeDao;
    private final RoleMapper roleMapper;
    private final UserDao userDao;
    private final TransactionUtils transactionUtils;

    @Transactional
    public RoleWithScopesResponse addRole(RoleCommand command) {
        List<Scope> scopes = scopeDao.findByIds(command.getScopeIds());

        if (scopes.size() < command.getScopeIds().size()) {
            List<Integer> foundIds = scopes.stream().map(Scope::getId).toList();
            throw new NoSuchEntityException("Scopes not found with ids: " +
                    command.getScopeIds().stream().filter(
                            o -> !foundIds.contains(o)
                    ).toList()
            );
        }

        Role role = roleDao.save(roleMapper.toEntity(command, scopes));

        transactionUtils.afterCommit(() ->
                log.info("Successfully added role with id: {}", role.getId())
        );

        return roleMapper.toResponseWithScopes(role);
    }

    @Transactional
    public RoleWithScopesResponse updateRole(int roleId, RoleCommand command) {
        Role role = roleDao.findById(roleId).orElseThrow(
                () -> new NoSuchEntityException("No role found with id: " + roleId)
        );
        List<Scope> scopes = scopeDao.findByIds(command.getScopeIds());
        List<Integer> foundIds = scopes.stream().map(Scope::getId).toList();

        if (scopes.size() < command.getScopeIds().size()) {
            throw new NoSuchEntityException("Scopes not found with ids: " +
                    command.getScopeIds().stream().filter(
                            o -> !foundIds.contains(o)
                    ).toList()
            );
        }

        roleDao.clearRoleScopes(roleId);
        roleDao.batchInsertRoleScopes(roleId, foundIds);
        roleDao.refresh(role); // Несмотря на два запроса, оказалось быстрее, чем запрашивать два JOIN-а

        transactionUtils.afterCommit(() ->
                log.info("Successfully updated role with id: {}", roleId)
        );

        return roleMapper.toResponseWithScopes(role);
    }

    @Transactional
    public void deleteRole(int roleId) {
        List<Integer> usersIdsWithRole = userDao.findIdsByRoleId(roleId);
        if (!usersIdsWithRole.isEmpty()) {
            throw new UnavailableActionException(
                    "Forbidden to delete role when there're users with it. User IDs:" + usersIdsWithRole
            );
        }

        log.info("Successfully deleted role with id: {}", roleId);
        roleDao.delete(roleId);
    }
}
