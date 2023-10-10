package com.codurance.katalyst.payment.application.ports.holded.exceptions;

public class NotValidEMailFormat extends RuntimeException {
    public NotValidEMailFormat(String message) {
        super(message);
    }
}
