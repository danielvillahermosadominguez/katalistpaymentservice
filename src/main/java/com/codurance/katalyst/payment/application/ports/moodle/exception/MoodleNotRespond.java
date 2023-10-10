package com.codurance.katalyst.payment.application.ports.moodle.exception;

public class MoodleNotRespond extends Throwable {
    private final String function;
    private final String endPoint;
    private final String requestBody;

    public MoodleNotRespond(String function, String endPoint,String requestBody, String message) {
        super(message);
        this.function = function;
        this.endPoint = endPoint;
        this.requestBody = requestBody;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String  getFunction() {
        return function;
    }

    public String  getEndPoint() {
        return endPoint;
    }
}
