package com.codurance.katalyst.payment.application.model.payment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentNotification {
    //Parameters in the documentation
    @JsonProperty("MethodId")
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    PaymentMethod methodId;
    @JsonProperty("MethodName")
    String methodName;
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @JsonProperty("TransactionType")
    TransactionType transactionType;
    @JsonProperty("TransactionName")
    String transactionName;
    @JsonProperty("CardCountry")
    String cardCountry;
    @JsonProperty("BankDateTime")
    String bankDateTime;
    @JsonProperty("ClearanceDateTime")
    String clearanceDateTime;
    @JsonProperty("Order")
    String order;
    @JsonProperty("Response")
    String response;
    @JsonProperty("ErrorID")
    String errorID;
    @JsonProperty("AuthCode")
    String authCode;
    @JsonProperty("Currency")
    String currency;
    @JsonProperty("Amount")
    String amount;
    @JsonProperty("AmountEur")
    String amountEur;
    @JsonProperty("TpvID")
    int tpvID;
    @JsonProperty("Concept")
    String concept;
    @JsonProperty("IdUser")
    String idUser;
    @JsonProperty("TokenUser")
    String tokenUser;
    @JsonProperty("SecurePayment")
    String securePayment;
    @JsonProperty("CardBrand")
    String cardBrand;
    @JsonProperty("BicCode")
    String bicCode;
    //@JsonProperty("Scoring") <-- We don't have it in the example, we need to review
    //String scoring;
    @JsonProperty("NotificationHash")
    String notificationHash;
    @JsonProperty("cardType")
    String cardType;
    @JsonProperty("cardCategory")
    String cardCategory;
    //@JsonProperty("paycometId") <-- We don't have it in the example, we need to review
    //long payCometId;

    //Parameters not in the documentation
    @JsonProperty("Signature")
    String signature;
    @JsonProperty("ErrorDescription")
    String errorDescription;
    @JsonProperty("Language")
    String language;
    @JsonProperty("AccountCode")
    String accountCode;
    @JsonProperty("DCC_Currency")
    String dCC_Currency;
    @JsonProperty("DCC_CurrencyName")
    String dCC_CurrencyName;
    @JsonProperty("DCC_Amount")
    String dCC_Amount;
    @JsonProperty("DCC_Markup")
    String dCC_Markup;
    @JsonProperty("DCC_ExchangeRate")
    String dCC_ExchangeRate;
    @JsonProperty("ExtendedSignature")
    String extendedSignature;
    @JsonProperty("sepaCard")
    String sepaCard;
    @JsonProperty("XPAYORIGIN")
    String xPAYORIGIN;

    public PaymentNotification() {

    }
    public PaymentNotification(PaymentMethod methodId,
                               TransactionType transactionType,
                               int tpvID,
                               String order,
                               String amount,
                               String response) {
        this.methodId = methodId;
        this.transactionType = transactionType;
        this.tpvID = tpvID;
        this.order = order;
        this.amount = amount;
        this.response = response;
    }

    public PaymentMethod getMethodId() {
        return methodId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setTransactionType(TransactionType value) {
        transactionType = value;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setMethod(PaymentMethod value) {
        methodId = value;
    }

    public void setTpvID(int value) {
        this.tpvID = value;
    }

    public int getTpvID() {
        return tpvID;
    }

    public String getOrder() {
        return this.order;
    }

    public String getAmount() {
    return this.amount;
    }

    public String getResponse() {
        return response;
    }
}
