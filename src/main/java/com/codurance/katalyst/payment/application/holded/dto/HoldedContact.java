package com.codurance.katalyst.payment.application.holded.dto;

public class HoldedContact {
    private String purchaseAccount;
    private HoldedBillAddress billAddress;
    private String phone;
    protected String id;
    protected String customId;
    protected String name;
    protected HoldedEmail email;
    protected String code;
    protected HoldedTypeContact type;

    public HoldedContact(String name,
                         String code,
                         HoldedTypeContact type,
                         HoldedEmail email,
                         String phone,
                         HoldedBillAddress billAddress,
                         String purchaseAccount) {

        this.customId = code + email.getValue();
        this.name = name;
        this.code = code;
        this.type = type;
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

    public String getName() {
        return name;
    }

    public HoldedEmail getEmail() {
        return email;
    }

    public String getCustomId() {
        return customId;
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

    public String getPurchaseAccount() {
        return purchaseAccount;
    }

    public void setCustomId(String customId) {
        this. customId = customId;
    }
}
