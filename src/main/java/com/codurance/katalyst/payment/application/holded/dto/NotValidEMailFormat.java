package com.codurance.katalyst.payment.application.holded.dto;

public class NotValidEMailFormat extends RuntimeException {
    public NotValidEMailFormat(String message) {
        super(message);
    }
}
