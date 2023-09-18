package com.codurance.katalyst.payment.application.holded.dto;

import java.util.List;

public class HoldedCreationDataInvoice {
    protected String id;
    public String getId() {
        return id;
    }

    protected List<HoldedCreationDataInvoiceItem> items;

    public List<HoldedCreationDataInvoiceItem> getItems(){
        return this.items;
    }
}
