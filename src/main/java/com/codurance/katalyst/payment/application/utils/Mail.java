package com.codurance.katalyst.payment.application.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Mail {
    public static final String AT = "@";
    public static final String SPECIAL_CHARACTERS = "[!#$%&'*+-/=?]";
    private final String email;

    public Mail(String email) {
        this.email = email;
    }

    public String getUserName() {
        String finalOutput = "";
        String arrayOfStr[] = email.split(AT);
        if (arrayOfStr.length == 2) {
            finalOutput = arrayOfStr[0];
        } else {
            //TODO: throw an exception
        }
        finalOutput = finalOutput.replaceAll(SPECIAL_CHARACTERS,"");

        return finalOutput;
    }

    public String getInUnicodeFormat() throws UnsupportedEncodingException {
        return URLEncoder.encode(email, "UTF-8");
    }
}
