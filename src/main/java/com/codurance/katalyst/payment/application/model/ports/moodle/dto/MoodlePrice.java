package com.codurance.katalyst.payment.application.model.ports.moodle.dto;

import java.util.regex.Pattern;

public class MoodlePrice {
    private double value;
    private String valueRaw;

    public MoodlePrice(String value) {
        var pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]*");
        this.value = 0.0;
        if(value == null || value.isEmpty()) {
            return;
        }
        valueRaw= value;
        var matcher = pattern.matcher(value);
        if (matcher.matches()) {
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
