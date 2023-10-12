package com.codurance.katalyst.payment.application.model.payment.entity;

import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;

import java.util.Objects;

public class PaymentTransaction {
    private PaymentStatus paymentStatus;
    private TransactionType transactionType;
    private int id;
    private String ip;
    private PaymentMethod method;
    private String tpvToken;
    private String tpvUser;
    private String order;
    private double amount;
    private String date;
    private PaymentTransactionState state;

    public PaymentTransaction(int id,
                              String ip,
                              PaymentMethod method,
                              TransactionType transactionType,
                              String tpvToken,
                              String tpvUser,
                              String order,
                              double amount,
                              String date,
                              PaymentTransactionState state,
                              PaymentStatus paymentStatus) {
        this.id = id;
        this.ip = ip;
        this.method = method;
        this.tpvToken = tpvToken;
        this.tpvUser = tpvUser;
        this.order = order;
        this.amount = amount;
        this.date = date;
        this.state = state;
        this.transactionType = transactionType;
        this.paymentStatus = paymentStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int value) {
        id = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentTransaction that = (PaymentTransaction) o;
        return id == that.id && Double.compare(that.amount, amount) == 0 && Objects.equals(ip, that.ip) && method == that.method && Objects.equals(tpvToken, that.tpvToken) && Objects.equals(tpvUser, that.tpvUser) && Objects.equals(order, that.order) && Objects.equals(date, that.date) && Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ip, method, tpvToken, tpvUser, order, amount, date, state);
    }

    public String getIp() {
        return ip;
    }

    public PaymentMethod getPaymentMethod() {
        return method;
    }

    public String getTpvUser() {
        return tpvUser;
    }

    public String getOrder() {
        return order;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public PaymentTransactionState getState() {
        return state;
    }

    public TransactionType getTransactionType() {
        return this.transactionType;
    }

    public String getTpvToken() {
        return tpvToken;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus value) {
        paymentStatus = value;
    }
}
