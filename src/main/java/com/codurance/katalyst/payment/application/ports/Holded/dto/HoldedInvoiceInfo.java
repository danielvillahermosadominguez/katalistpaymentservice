package com.codurance.katalyst.payment.application.ports.Holded.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HoldedInvoiceInfo {

    @JsonProperty
    protected String id;

    public HoldedInvoiceInfo() {

    }

    public HoldedInvoiceInfo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
