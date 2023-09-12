package com.codurance.katalyst.payment.katalistpayment.utils;

import com.codurance.katalyst.payment.katalistpayment.moodle.MoodleCustomField;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringToDouble {
    private final String value;

    public StringToDouble(String value) {
        this.value = value;
    }

    public double convert() {
        Pattern pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]*");
        Matcher matcher = pattern.matcher(value);
        if(!matcher.matches()) {
            return 0.0;
        }
        double result = Double.parseDouble(value);
        return result;
    }
}
