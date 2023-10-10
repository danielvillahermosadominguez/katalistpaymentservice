package com.codurance.katalyst.payment.application.ports.holded.dto;

public class HoldedStatus {
    public static final int OK = 1;
    protected int status;
    protected String info;

    protected String id;

    public HoldedStatus() {

    }

    public HoldedStatus(int status, String info, String id) {
        this.status = status;
        this.info = info;
        this.id = id;
    }

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
