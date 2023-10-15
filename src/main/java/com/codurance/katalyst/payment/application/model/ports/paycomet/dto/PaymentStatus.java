package com.codurance.katalyst.payment.application.model.ports.paycomet.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentStatus {

    @JsonProperty("errorCode")
    private int errorCode;

    @JsonProperty("amount")
    private int amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("order")
    private String order;

    @JsonProperty("methodId")
    private int methodId;
    @JsonProperty("challengeUrl")
    private String challengeUrl;

    @JsonProperty("authCode")
    private String authCode;

    @JsonProperty("idUser")
    private int idUser;

    @JsonProperty("tokenUser")
    private  String tokenUser;

    @JsonProperty("cardCountry")
    private String cardCountry;

    @JsonIgnore
    private String methodData;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getChallengeUrl() {
        return challengeUrl;
    }

    public void setChallengeUrl(String challengeUrl) {
        this.challengeUrl = challengeUrl;
    }

    public void setOrder(String value) {
        this.order = value;
    }

    public String getOrder() {
        return order;
    }
}
