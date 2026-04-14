package com.chump.user.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
public class UserProfileResponse {

    private Integer id;
    private String username;
    private String fullName;
    private LocalDate dateOfBirth;
    private BigDecimal balance;
    private BigDecimal discount;
    private RoleResponse role;
}