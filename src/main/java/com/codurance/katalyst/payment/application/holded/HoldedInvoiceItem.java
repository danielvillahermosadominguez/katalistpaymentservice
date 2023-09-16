package com.codurance.katalyst.payment.application.holded;

public class HoldedInvoiceItem {
    private String name;
    private int units;
    private double subtotal;

    public HoldedInvoiceItem(String name, int units, double subtotal) {
        this.name = name;
        this.units = units;
        this.subtotal = subtotal;
    }
}
