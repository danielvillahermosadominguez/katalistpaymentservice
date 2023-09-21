package com.codurance.katalyst.payment.application.paycomet.dto;

public class PaymentStatus {
    private int errorCode;
    private int amount;
    private String currency;

    private String order;

    private String challengeUrl;

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
}
