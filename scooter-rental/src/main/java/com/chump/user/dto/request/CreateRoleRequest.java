package com.chump.user.dto.request;

import com.chump.common.validation.Trimmed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateRoleRequest {

    @Trimmed(message = "Field 'name' must not contain trailing spaces")
    @NotBlank(message = "Field 'name' must not be empty")
    private String name;

    @NotEmpty(message = "List 'scopeIds' should not be empty")
    private List<Integer> scopeIds;
}
