package com.codurance.katalyst.payment.application.actions.exception;

public class InvalidInputCustomerData extends Throwable{
    public InvalidInputCustomerData(String message) {
        super(message);
    }
}
