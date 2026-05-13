package com.chump.emulator;

import java.util.List;
import java.util.Map;
import java.util.Stack;

public class TestRoutes {

    public static Map.Entry<Double, Double> END_IN_PARKING_START = Map.entry(37.5469596, 55.7494444);
    public static Stack<Map.Entry<Double, Double>> END_IN_PARKING = new Stack<>();
    static {
        END_IN_PARKING.addAll(List.of(
                Map.entry(37.5411343, 55.7524297),
                Map.entry(37.54045361, 55.75222331),
                Map.entry(37.54058771, 55.75205121),
                Map.entry(37.54077011, 55.75206031),
                Map.entry(37.54115371, 55.75213181),
                Map.entry(37.54159181, 55.75169621),
                Map.entry(37.54209451, 55.75120351),
                Map.entry(37.54269911, 55.75063421),
                Map.entry(37.54311751, 55.75024481),
                Map.entry(37.54354961, 55.74984121),
                Map.entry(37.54400561, 55.74940341),
                Map.entry(37.54437391, 55.74904891),
                Map.entry(37.54506591, 55.74851151),
                Map.entry(37.54603151, 55.74900361),
                Map.entry(37.54695961, 55.74944441)
        ));
    }

    public static Map.Entry<Double, Double> END_NOT_IN_PARKING_START = Map.entry(37.5469596, 55.7494444);
    public static Stack<Map.Entry<Double, Double>> END_NOT_IN_PARKING = new Stack<>();
    static {
        END_NOT_IN_PARKING.addAll(List.of(
                Map.entry(37.5404536, 55.7522233),
                Map.entry(37.5405877, 55.7520512),
                Map.entry(37.5407701, 55.7520603),
                Map.entry(37.5411537, 55.7521318),
                Map.entry(37.5415918, 55.7516962),
                Map.entry(37.5420945, 55.7512035),
                Map.entry(37.5426991, 55.7506342),
                Map.entry(37.5431175, 55.7502448),
                Map.entry(37.5435496, 55.7498412),
                Map.entry(37.5440056, 55.7494034),
                Map.entry(37.5443739, 55.7490489),
                Map.entry(37.5450659, 55.7485115),
                Map.entry(37.5460315, 55.7490036),
                Map.entry(37.5469596, 55.7494444)
        ));
    }
}
