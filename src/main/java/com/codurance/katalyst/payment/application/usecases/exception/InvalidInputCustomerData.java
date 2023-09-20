package com.codurance.katalyst.payment.application.usecases.exception;

public class InvalidInputCustomerData extends Throwable{
    public InvalidInputCustomerData(String message) {
        super(message);
    }
}
