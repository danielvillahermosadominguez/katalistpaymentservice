package com.codurance.katalyst.payment.application.ports.moodle.dto;

import java.util.regex.Pattern;

public class MoodlePrice {
    private final double value;
    private final String valueRaw;

    public MoodlePrice(String value) {
        valueRaw= value;
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

    public String getValueRaw() {
        return valueRaw;
    }
}
