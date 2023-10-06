package com.codurance.katalyst.payment.application.holded.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HoldedContact {
    @JsonProperty("isperson")
    private boolean isPerson;

    protected String id;

    protected String customId;

    protected String name;

    protected String code;
    private String phone;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    protected HoldedEmail email;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    protected HoldedTypeContact type;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private HoldedBillAddress billAddress;

    @JsonIgnore
    private String purchaseAccount;

    public HoldedContact() {

    }

    public HoldedContact(String name,
                         String code,
                         HoldedTypeContact type,
                         boolean isPerson,
                         HoldedEmail email,
                         String phone,
                         HoldedBillAddress billAddress,
                         String purchaseAccount) {

        this.customId = code + email.getInUnicodeFormat();
        this.name = name;
        this.code = code;
        this.type = type;
        this.isPerson = isPerson;
        this.email = email;
        this.phone = phone;
        this.billAddress = billAddress;
        this.purchaseAccount = purchaseAccount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomId() {
        return customId;
    }

    public String getName() {
        return name;
    }

    public HoldedEmail getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }

    public HoldedTypeContact getType() {
        return type;
    }

    public String getPhone() {
        return phone;
    }

    public HoldedBillAddress getBillAddress() {
        return billAddress;
    }

    @JsonIgnore
    public String getPurchaseAccount() {
        return purchaseAccount;
    }

    public void setCustomId(String customId) {
        this. customId = customId;
    }

    public boolean isPerson() {
        return isPerson;
    }
}
