package com.chump.tariff.dto.response;

import com.chump.tariff.model.TariffType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class TariffResponse {

    private Integer id;
    private String name;
    private BigDecimal basePrice;
    private TariffType type;
}
