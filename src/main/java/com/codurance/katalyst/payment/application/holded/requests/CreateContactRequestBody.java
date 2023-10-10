package com.codurance.katalyst.payment.application.holded.requests;

import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedBillAddress;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedContact;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


public class CreateContactRequestBody {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private final HoldedBillAddress billAddress;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("type")
    private String type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("vatnumber")
    private String vatNumber;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("code")
    private String code;

    @JsonProperty("CustomId")
    private String customId;

    @JsonProperty("isperson")
    private boolean isPerson;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("phone")
    private final String phone;


    public CreateContactRequestBody(HoldedContact contact) {
        var email = contact.getEmail().getValue();
        this.name = contact.getName();
        this.email = email;
        this.type = contact.getType().getName();
        this.customId = contact.getCustomId();
        this.isPerson = contact.isPerson();
        this.code = contact.getCode();
        this.vatNumber = contact.getVatNumber();
        this.phone = contact.getPhone();
        this.billAddress = contact.getBillAddress();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public boolean getIsPerson() {
        return isPerson;
    }

    public void setIsPerson(boolean isPerson) {
        this.isPerson = isPerson;
    }
}
