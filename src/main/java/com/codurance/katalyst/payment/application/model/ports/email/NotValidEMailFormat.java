package com.codurance.katalyst.payment.application.model.ports.email;

public class NotValidEMailFormat extends RuntimeException {
    public NotValidEMailFormat(String message) {
        super(message);
    }
}
