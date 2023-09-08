package com.codurance.katalyst.payment.katalistpayment.holded;

public class HoldedContactDTO {
    private String id;
    private String customId;
    private String name;
    private String email;
    private String code;
    private String type;

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
}
