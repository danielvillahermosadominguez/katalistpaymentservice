package com.codurance.katalyst.payment.application.model.ports.paycomet.exception;

public class PayCometNotRespond extends Throwable{
    private final String url;
    private final String urlVariables;
    private final String requestBody;

    public PayCometNotRespond(String url, String urlVariables, String requestBody, String message) {
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
