package com.chump.auth.mapper;

import com.chump.auth.dto.command.LoginCommand;
import com.chump.auth.dto.command.RegisterCommand;
import com.chump.auth.dto.request.LoginRequest;
import com.chump.auth.dto.request.RegisterRequest;
import com.chump.user.model.Role;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(source = "command.fullName", target = "fullName")
    @Mapping(source = "command.dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "command.balance", target = "balance") // TODO может заменить на DEFAULT в БД
    @Mapping(source = "user", target = "user")
    @Mapping(target = "discount", ignore = true) // Изначально скидки нет
    UserProfile toUserProfileEntity(RegisterCommand command, User user);

    @Mapping(source = "command.username", target = "username")
    @Mapping(target = "password", ignore = true) // Маппер не должен знать о логике хэширования
    @Mapping(source = "role", target = "role")
    User toUserEntity(RegisterCommand command, Role role);

    LoginCommand toLoginCommand(LoginRequest loginRequest);

    @Mapping(target = "balance", expression = "java(BigDecimal.ZERO)")
    RegisterCommand toRegisterCommand(RegisterRequest registerRequest);
}
