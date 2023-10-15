package com.codurance.katalyst.payment.application.infrastructure.adapters.paycomet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentBody {

    @JsonProperty("payment")
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private PaymentParams payment;

    public PaymentParams getPayment() {
        return payment;
    }

    public void setPayment(PaymentParams payment) {
        this.payment = payment;
    }
}
