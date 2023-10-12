package com.codurance.katalyst.payment.application.model.payment.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {
    AUTHORIZATION(1),
    REFUND(2),
    PREAUTORIZATION(3),
    PREAUTORIZATION_CANCELATION(4),
    PREAUTORIZATION_CONFIRMATION(6),
    SUBSCRIPTION(9),
    DEFERED_PREAHTHORIZATION_REGISTRATION(13),
    DEFERED_PREAHTHORIZATION_CONFIRMATION(14),
    DEFERED_PREAUTORIZATION_CANCELLATION(16),
    DENIAL_OF_SEPA_CREDIT_TRANSFER(30),
    CHARGEBACK(106),
    BANKSTORE_USER_REGISTRATION(107),
    CHARGES_TO_A_USER_REFERENCE(114),
    WITHDRAWAL_DENIAL(116);

    private int value;
    TransactionType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return this.value;
    }
}
