package com.codurance.katalyst.payment.application.holded.dto;

public class HoldedStatus {
    public static final int OK = 1;
    protected int status;
    protected String info;

    protected String id;

    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }

    public String getId() {
        return id;
    }
}
