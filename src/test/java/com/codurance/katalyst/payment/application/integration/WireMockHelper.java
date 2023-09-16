package com.codurance.katalyst.payment.application.integration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class WireMockHelper {
    public static final String EQUAL_SYMBOL = "=";
    public static final String JOIN_SYMBOL = "&";

    protected String joinParameters(Map<String, String> requestBodyMap) throws UnsupportedEncodingException {
        String requestBody = "";

        for (Map.Entry<String, String> parameter : requestBodyMap.entrySet()) {
            requestBody += unicode(parameter.getKey()) + EQUAL_SYMBOL + unicode(parameter.getValue()) + JOIN_SYMBOL;
        }
        if (!requestBody.isEmpty()) {
            requestBody = requestBody.substring(0, requestBody.length() - 1);
        }
        return requestBody;
    }

    protected String unicode(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }
}
