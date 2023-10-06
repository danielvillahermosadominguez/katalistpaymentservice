package com.codurance.katalyst.payment.application.holded.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HoldedTypeContact {
    Supplier("supplier"),
    Debtor ("debtor"),
    Creditor ("creditor"),
    Client ("client"),
    Lead ("lead");

    private final String value;

    HoldedTypeContact(String value) {
        this.value = value;
    }
    @JsonValue
    public String getName() {
        return this.value;
    }
}
