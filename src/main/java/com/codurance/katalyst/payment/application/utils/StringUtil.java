package com.codurance.katalyst.payment.application.utils;

import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;
import com.google.gson.Gson;

import java.util.Map;
import java.util.regex.Pattern;

public class StringUtil {
    private final String value;

    public StringUtil(String value) {
        this.value = value;
    }

    public double tryToConvertToDouble() {
        var pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]*");
        var matcher = pattern.matcher(value);
        if(!matcher.matches()) {
            return 0.0;
        }
        return Double.parseDouble(value);
    }
}
