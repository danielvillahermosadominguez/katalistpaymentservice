package com.codurance.katalyst.payment.application.usecases;

public class InvalidInputCustomerData extends Throwable{
    public InvalidInputCustomerData(String message) {
        super(message);
    }
}
