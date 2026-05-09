package com.chump.common.utils;

import com.chump.rental.model.Trip;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TripPriceCalculator {

    public static BigDecimal calculatePrice(Trip trip) {
        BigDecimal price = trip.getPriceAtStart();
        Integer interval = trip.getIntervalAtStart();
        BigDecimal discount = trip.getDiscountAtStart();

        BigDecimal fullPrice = null;

        if (interval != null) {
            fullPrice = price
                    .multiply(
                            BigDecimal.valueOf(trip.getDurationSeconds())
                                    .divide(BigDecimal.valueOf(60L * interval), 0, RoundingMode.UP));
            fullPrice = fullPrice.subtract(fullPrice.multiply(discount));
        }
        return fullPrice;
    }
}
