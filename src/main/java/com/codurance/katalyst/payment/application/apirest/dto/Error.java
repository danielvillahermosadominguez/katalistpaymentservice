package com.codurance.katalyst.payment.application.apirest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Error {
    public static final int ERROR_CODE_COURSE_DOESNT_EXIST = 1;
    public static final int CODE_ERROR_USER_HAS_ALREADY_A_SUSCRIPTION_TO_THIS_COURSE = 2;
    public static final int CODE_ERROR_GENERAL_SUBSCRIPTION = 3;
    public static final int CODE_ERROR_PRICE_NOT_FOUND = 4;
    public static final int ERROR_PAYMENT_PLATFORM_CANNOT_TO_PROCESS_THIS_CREDIT_CARD = 5;

    private int code;
    private String message;

    public Error() {

    }

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

    @JsonIgnore
    public String getFullMessage() {
        return String.format("[ERROR CODE] = %s, [MESSAGE] = %s",code,message );
    }
}
