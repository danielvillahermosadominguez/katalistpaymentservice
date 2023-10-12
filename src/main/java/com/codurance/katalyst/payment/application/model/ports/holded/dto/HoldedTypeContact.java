package com.codurance.katalyst.payment.application.model.ports.holded.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HoldedTypeContact {
    SUPPLIER("supplier"),
    DEBTOR("debtor"),
    CREDITOR("creditor"),
    CLIENT("client"),
    LEAD("lead");

    private final String value;

    HoldedTypeContact(String value) {
        this.value = value;
    }
    @JsonValue
    public String getName() {
        return this.value;
    }
}
