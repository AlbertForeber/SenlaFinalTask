package com.chump.rental.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class RentalSpotHierarchyResponse {

    private Integer id;
    private String name;
    private List<RentalSpotHierarchyResponse> children;
}
