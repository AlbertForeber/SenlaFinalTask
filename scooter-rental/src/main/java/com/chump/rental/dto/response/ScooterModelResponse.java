package com.chump.rental.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScooterModelResponse {

    private Integer id;
    private String vendor;
    private String name;
}
