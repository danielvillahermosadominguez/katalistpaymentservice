package com.codurance.katalyst.payment.application.model.ports.holded.exceptions;

public class HoldedNotRespond extends Throwable {
    private final String url;
    private final String urlVariables;
    private String requestBody;

    public HoldedNotRespond(String url, String urlVariables, String requestBody, String message) {
        super(message);
        this.url = url;
        this.urlVariables = urlVariables;
        this.requestBody = requestBody;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlVariables() {
        return urlVariables;
    }

    public String getRequestBody() {
        return requestBody;
    }
}
