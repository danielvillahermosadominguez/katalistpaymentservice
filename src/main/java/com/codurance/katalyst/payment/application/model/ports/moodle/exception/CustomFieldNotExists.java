package com.codurance.katalyst.payment.application.model.ports.moodle.exception;

public class CustomFieldNotExists extends Throwable {
    public CustomFieldNotExists(String message) {
        super(message);
    }
}
