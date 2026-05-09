package com.chump.user.dto.request;

import com.chump.common.validation.Trimmed;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateRoleRequest {

    @Trimmed(message = "Field 'name' must not contain trailing spaces")
    private String name;

    @Size(message = "Scopes IDs list must not be empty")
    private List<Integer> scopeIds;
}
