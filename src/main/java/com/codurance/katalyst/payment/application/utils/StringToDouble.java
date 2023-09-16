package com.codurance.katalyst.payment.application.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringToDouble {
    private final String value;

    public StringToDouble(String value) {
        this.value = value;
    }

    public double convert() {
        var pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]*");
        var matcher = pattern.matcher(value);
        if(!matcher.matches()) {
            return 0.0;
        }
        return Double.parseDouble(value);
    }
}
