package com.gmail.etpr99.jose.coviddailytable.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {
    public static double round(double num, int scale) {
        BigDecimal bd = new BigDecimal(num);
        BigDecimal roundOff = bd.setScale(scale, RoundingMode.HALF_EVEN);
        return roundOff.doubleValue();
    }

    public static double formatStatPercentVariation(double startPercentVariation) {
        if (startPercentVariation >= 1.0) {
            return Math.abs(round((1 - startPercentVariation) * 100, 2));
        } else {
            return -round((1 - startPercentVariation) * 100, 2);
        }
    }
}
