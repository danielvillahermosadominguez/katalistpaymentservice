package com.codurance.katalyst.payment.application.utils;

public class NotValidEMailFormat extends Throwable {
    public NotValidEMailFormat(String message) {
        super(message);
    }
}
