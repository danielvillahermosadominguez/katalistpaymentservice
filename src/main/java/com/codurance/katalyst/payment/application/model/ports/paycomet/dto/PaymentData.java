package com.codurance.katalyst.payment.application.model.ports.paycomet.dto;

public class PaymentData {
    private double amount;
    private String currency;
    private int idUser;
    private int methodId;
    private String order;
    private String originalIp;
    private String tokenUser;

    public PaymentData(double amount,
                       String currency,
                       int idUser,
                       int methodId,
                       String order,
                       String originalIPm,
                       String tokenUser) {
        this.amount = amount;
        this.currency = currency;
        this.idUser = idUser;
        this.order = order;
        this.originalIp = originalIPm;
        this.tokenUser = tokenUser;
        this.methodId = methodId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getIdUser() {
        return idUser;
    }

    public int getMethodId() {
        return methodId;
    }

    public String getOrder() {
        return order;
    }

    public String getOriginalIp() {
        return originalIp;
    }

    public String getTokenUser() {
        return tokenUser;
    }

    public boolean getDate() {
        return false;
    }
}
