package com.codurance.katalyst.payment.application.holded.requests;

public class HoldedInvoiceStatus {
    public static final int OK = 1;

    protected int status;
    protected String invoiceNum;

    protected String id;

    public int getStatus() {
        return status;
    }

    public String getInvoiceNum() {
        return invoiceNum;
    }

    public String getId() {
        return id;
    }
}
