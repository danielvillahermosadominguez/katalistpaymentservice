package com.codurance.katalyst.payment.application.model.payment.entity;

public enum PaymentTransactionState {
    PENDING("Pending"),
    RETRY("Retry"),
    DONE("Done");

    private final String value;

    PaymentTransactionState(String value) {
        this.value = value;
    }

    public static PaymentTransactionState fromStr(String value) {
        for (PaymentTransactionState state : PaymentTransactionState.values()) {
            if (state.getValue().equals(value)) {
                return state;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
