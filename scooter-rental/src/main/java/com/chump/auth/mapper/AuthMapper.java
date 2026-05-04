package com.chump.auth.mapper;

import com.chump.auth.dto.command.LoginCommand;
import com.chump.auth.dto.command.RegisterCommand;
import com.chump.auth.dto.request.LoginRequest;
import com.chump.auth.dto.request.RegisterRequest;
import com.chump.auth.dto.response.SessionResponse;
import com.chump.auth.model.Session;
import com.chump.user.model.Role;
import com.chump.user.model.User;
import com.chump.user.model.UserProfile;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

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

    @Mapping(source = "request.username", target = "username")
    @Mapping(source = "request.password", target = "password")
    @Mapping(source = "deviceName", target = "deviceName")
    @Mapping(source = "ipAddress", target = "ipAddress")
    LoginCommand toLoginCommand(LoginRequest request, String deviceName, String ipAddress);

    @InheritConfiguration(name = "toLoginCommand")
    @Mapping(source = "request.dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "request.fullName", target = "fullName")
    @Mapping(target = "balance", expression = "java(BigDecimal.ZERO)")
    RegisterCommand toRegisterCommand(RegisterRequest request, String deviceName, String ipAddress);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "createdAt", target = "lastActiveAt")
    SessionResponse toSessionResponse(Session entity);

    List<SessionResponse> toSessionResponseList(List<Session> entityList);
}
