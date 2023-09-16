package com.codurance.katalyst.payment.application.api;

public class Error {
    public static final int ERROR_CODE_COURSE_DOESNT_EXIST = 1;
    public static final int CODE_ERROR_USER_HAS_ALREADY_A_SUSCRIPTION_TO_THIS_COURSE = 2;
    public static final int CODE_ERROR_PROBLEM_WITH_MOODLE = 3;
    public static final int CODE_ERROR_PRICE_NOT_FOUND = 4;
    private int code;
    private String message;

    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
