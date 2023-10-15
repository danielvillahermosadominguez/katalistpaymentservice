package com.codurance.katalyst.payment.application.infrastructure.adapters.paycomet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentParams {

    @JsonProperty("terminal")
    private int terminal;
    @JsonProperty("amount")
    private String amount;
    @JsonProperty("currency")
    private String currency = "EUR";
    @JsonProperty("idUser")
    private int idUser;
    @JsonProperty("methodId")
    private String methodId = "1";
    @JsonProperty("originalIp")
    private String originalIp;
    @JsonProperty("secure")
    private int secure = 1; //1 = secure 0 = no secure
    @JsonProperty("order")
    private String order;
    @JsonProperty("tokenUser")
    private String tokenUser;
    @JsonProperty("productDescription")
    private String productDescription = "Katalyst subscription";
    @JsonProperty("merchantDescriptor")
    private String merchantDescriptor = "Katalyst subscription";

    public int getTerminal() {
        return terminal;
    }

    public void setTerminal(int terminal) {
        this.terminal = terminal;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getIdUser() {
                return idUser;
        }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getSecure() {
        return secure;
    }

    public void setSecure(int secure) {
        this.secure = secure;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getTokenUser() {
        return tokenUser;
    }

    public void setTokenUser(String tokenUser) {
        this.tokenUser = tokenUser;
    }

    public String getOriginalIp() {
        return originalIp;
    }

    public void setOriginalIp(String originalIp) {
        this.originalIp = originalIp;
    }
}
