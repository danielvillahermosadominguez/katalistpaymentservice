package com.codurance.katalyst.payment.application.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class EMail {
    public static final String AT = "@";
    public static final String SPECIAL_CHARACTERS = "[!#$%&'*+-/=?]";
    private final String value;

    public EMail(String email) {
        this.value = email;
    }

    public String getUserName() {
        String finalOutput = "";
        String arrayOfStr[] = value.split(AT);
        if (arrayOfStr.length == 2) {
            finalOutput = arrayOfStr[0];
        } else {
            //TODO: throw an exception
        }
        finalOutput = finalOutput.replaceAll(SPECIAL_CHARACTERS,"");

        return finalOutput;
    }

    public String getInUnicodeFormat() throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    public String getValue() {
        return value;
    }
}
