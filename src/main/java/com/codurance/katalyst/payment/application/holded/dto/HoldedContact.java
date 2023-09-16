package com.codurance.katalyst.payment.application.holded.dto;

public class HoldedContact {
    protected String id;
    protected String customId;
    protected String name;
    protected String email;
    protected String code;
    protected String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCustomId() {
        return customId;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }
}
