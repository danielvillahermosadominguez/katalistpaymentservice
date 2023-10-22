package com.codurance.katalyst.payment.application.integration.wiremock;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WireMockServerExtension {
    private static final String EQUAL_SYMBOL = "=";
    private static final String JOIN_SYMBOL = "&";

    protected MockServer wireMockServer = null;

    protected ObjectMapper objectMapper = new ObjectMapper();
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public void reset() {
        this.wireMockServer.getClient().resetRequests();
    }

    public void stop() {
        this.wireMockServer.stop();
    }

    public void start() {
        this.wireMockServer = new MockServer(options().port(this.port));
        this.wireMockServer.start();
    }

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
