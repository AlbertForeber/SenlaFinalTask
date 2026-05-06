package com.chump.user.mapper;

import com.chump.user.dto.command.UpdateUserBaseInfoCommand;
import com.chump.user.dto.command.UpdateUserProtectedInfoCommand;
import com.chump.user.dto.request.UpdateUserBaseInfoRequest;
import com.chump.user.dto.request.UpdateUserProtectedInfoRequest;
import com.chump.user.dto.response.UserProfileResponse;
import com.chump.user.dto.response.UserRoleResponse;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    UpdateUserProtectedInfoCommand toProtectedInfoCommand(UpdateUserProtectedInfoRequest request);
    UpdateUserBaseInfoCommand toBaseInfoCommand(UpdateUserBaseInfoRequest request);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.role", target = "role")
    @Mapping(source = "user.username", target = "username")
    UserProfileResponse toUserProfileResponse(UserProfile entity);

    UserRoleResponse toUserRoleResponse(User entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserBaseInfoFromCommand(UpdateUserBaseInfoCommand command, @MappingTarget UserProfile info);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserProtectedInfoFromCommand(UpdateUserProtectedInfoCommand command, @MappingTarget UserProfile info);
}