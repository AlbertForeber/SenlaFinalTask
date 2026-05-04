package com.chump.auth.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
public class SessionResponse {

    private Integer id;
    private Integer userId;
    private String deviceName;
    private String ipAddress;
    private Instant lastActiveAt;
}
