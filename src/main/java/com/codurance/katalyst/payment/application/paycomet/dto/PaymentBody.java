package com.codurance.katalyst.payment.application.paycomet.dto;

public class PaymentBody {
    private PaymentParams payment;

    public PaymentParams getPayment() {
        return payment;
    }

    public void setPayment(PaymentParams payment) {
        this.payment = payment;
    }
}
