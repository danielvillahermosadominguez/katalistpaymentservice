package com.codurance.katalyst.payment.application.model.purchase;

public class Purchase {
    private int id;
    private final int transactionId;

    public Purchase(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getId() {
        return id;
    }
}
