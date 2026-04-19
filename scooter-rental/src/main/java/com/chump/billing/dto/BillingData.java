package com.chump.billing.dto;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.SqlResultSetMapping;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@SqlResultSetMapping(
        name = "BillingDataMapping",
        classes = @ConstructorResult(
                targetClass = BillingData.class,
                columns = {
                        @ColumnResult(name = "basePrice", type = BigDecimal.class),
                        @ColumnResult(name = "durationDays", type = Integer.class)
                }
        )
)
public class BillingData {

    private BigDecimal basePrice;
    private Integer durationDays;
}
