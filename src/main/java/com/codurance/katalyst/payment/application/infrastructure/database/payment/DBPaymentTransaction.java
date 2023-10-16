package com.codurance.katalyst.payment.application.infrastructure.database.payment;

import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "PAYMENT_TRANSACTIONS")
public class DBPaymentTransaction {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "ip")
    private String ip;
    @Column(name = "payment_method")
    private int paymentMethod;
    @Column(name = "tpv_user")
    private int tpvUser;
    @Column(name = "tpv_token")
    private String tpvToken;
    @Column(name = "transaction_type")
    private int transactionType;
    @Column(name = "order_code")
    private String orderCode;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "date")
    private String date;
    @Column(name = "transaction_state")
    private String transactionState;
    @Column(name = "status_error_code")
    private int statusErrorCode;
    @Column(name = "status_amount")
    private int statusAmount;
    @Column(name = "status_currency")
    private String statusCurrency;
    @Column(name = "status_order")
    private String status_order;
    @Column(name = "status_challenge_url")
    private String statusChallengeUrl;

    public DBPaymentTransaction(PaymentTransaction paymentTransaction) {
        this.id = paymentTransaction.getId();
        update(paymentTransaction);
    }

    public DBPaymentTransaction() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void update(PaymentTransaction paymentTransaction) {
        this.ip = paymentTransaction.getIp();
        this.paymentMethod = paymentTransaction.getPaymentMethod().getValue();
        this.transactionType = paymentTransaction.getTransactionType().getValue();
        this.tpvToken = paymentTransaction.getTpvToken();
        this.tpvUser = paymentTransaction.getTpvUser();
        this.orderCode = paymentTransaction.getOrder();
        this.amount = BigDecimal.valueOf(paymentTransaction.getAmount());
        this.date = paymentTransaction.getDate();
        this.transactionState = paymentTransaction.getTransactionState().getValue();
        var paymentStatus = paymentTransaction.getPaymentStatus();
        this.statusAmount = paymentStatus.getAmount();
        this.statusCurrency = paymentStatus.getCurrency();
        this.statusErrorCode = paymentStatus.getErrorCode();
        this.statusChallengeUrl = paymentStatus.getChallengeUrl();
        this.status_order = paymentStatus.getOrder();
    }

    public PaymentTransaction toPaymentTransaction() {
        var paymentStatus = new PaymentStatus();
        paymentStatus.setAmount(this.statusAmount);
        paymentStatus.setOrder(this.status_order);
        paymentStatus.setCurrency(this.statusCurrency);
        paymentStatus.setErrorCode(this.statusErrorCode);
        paymentStatus.setChallengeUrl(this.statusChallengeUrl);

        return new PaymentTransaction(
                this.id,
                this.ip,
                PaymentMethod.fromInt(this.paymentMethod),
                TransactionType.fromInt(this.transactionType),
                this.tpvToken,
                this.tpvUser,
                this.orderCode,
                this.amount.doubleValue(),
                this.date,
                PaymentTransactionState.fromStr(this.transactionState),
                paymentStatus
        );
    }

    public String getTransactionState() {
        return this.transactionState;
    }
}
