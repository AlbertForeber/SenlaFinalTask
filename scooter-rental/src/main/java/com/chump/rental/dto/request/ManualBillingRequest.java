package com.chump.rental.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualBillingRequest {

    private boolean failedOnly = false;
}
