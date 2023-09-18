package com.codurance.katalyst.payment.application.holded.dto;

public class HoldedCreationDataInvoiceItem {
    protected String name;

    protected String desc;

    protected int units;
    protected double subtotal;

    public HoldedCreationDataInvoiceItem(String name, String desc, int units, double subtotal) {
        this.name = name;
        this.units = units;
        this.subtotal = subtotal;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getUnits() {
        return units;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
