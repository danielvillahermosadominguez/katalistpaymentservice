package com.codurance.katalyst.payment.application.holded.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CreateInvoiceRequestBody {
    @JsonProperty
    private String docType = "invoice";
    @JsonProperty
    private String contactId;
    @JsonProperty
    private String desc;
    @JsonProperty
    private long date;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @JsonProperty
    protected List<CreateInvoiceItemRequestBody> items;

    public CreateInvoiceRequestBody(String contactId, String desc, long date, List<CreateInvoiceItemRequestBody> items) {
        this.contactId = contactId;
        this.desc = desc;
        this.date = date;
        this.items = items;
    }
}
