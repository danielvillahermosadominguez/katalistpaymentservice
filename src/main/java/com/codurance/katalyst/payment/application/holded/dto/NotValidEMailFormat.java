package com.codurance.katalyst.payment.application.holded.dto;

public class NotValidEMailFormat extends Throwable {
    public NotValidEMailFormat(String message) {
        super(message);
    }
}
