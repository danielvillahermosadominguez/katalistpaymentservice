package com.codurance.katalyst.payment.application.model.payment;

public class PaymentTransaction {
    private int id;

    public PaymentTransaction(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int value) {
        id = value;
    }
}
