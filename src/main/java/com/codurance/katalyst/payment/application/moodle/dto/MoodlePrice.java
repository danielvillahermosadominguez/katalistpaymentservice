package com.codurance.katalyst.payment.application.moodle.dto;

import java.util.regex.Pattern;

public class MoodlePrice {
    private final double value;

    public MoodlePrice(String value) {
        var pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]*");
        var matcher = pattern.matcher(value);
        if (!matcher.matches()) {
            this.value = 0.0;
        } else {
            this.value = Double.parseDouble(value);
        }
    }

    public double getValue() {
        return value;
    }
}
