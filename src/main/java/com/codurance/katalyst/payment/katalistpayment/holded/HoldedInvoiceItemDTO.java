package com.codurance.katalyst.payment.katalistpayment.holded;

public class HoldedInvoiceItemDTO {
    private String name;
    private int units;
    private double subtotal;

    public HoldedInvoiceItemDTO(String name, int units, double subtotal) {
        this.name = name;
        this.units = units;
        this.subtotal = subtotal;
    }
}
