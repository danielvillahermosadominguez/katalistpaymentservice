package com.codurance.katalyst.payment.application.holded.requests;

import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.holded.dto.NotValidEMailFormat;
import com.fasterxml.jackson.annotation.JsonProperty;


public class CreateContactRequestBody {
    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("type")
    private String type;

    @JsonProperty("type")
    private String code;

    @JsonProperty("CustomId")
    private String customId;

    @JsonProperty("isperson")
    private String isPerson;

    public CreateContactRequestBody(HoldedContact contact) {
        var email = contact.getEmail().getValue();
        this.name = contact.getName();
        this.email = email;
        this.type = contact.getType().toString();
        this.code = contact.getCode();
        this.customId = contact.getCustomId();
        this.isPerson = "true";
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

    public String getIsPerson() {
        return isPerson;
    }

    public void setIsPerson(String isPerson) {
        this.isPerson = isPerson;
    }
}
