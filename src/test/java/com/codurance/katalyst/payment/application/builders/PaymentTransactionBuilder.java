package com.codurance.katalyst.payment.application.builders;

import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentTransactionBuilder {
    PaymentTransaction item;

    public PaymentTransactionBuilder createWithDefaultValues() {
        item = createPaymentTransaction();
        return this;
    }

    public PaymentTransaction getItem() {
        return item;
    }


    private PaymentTransaction createPaymentTransaction() {
        var paymentStatus = new PaymentStatus();
        paymentStatus.setErrorCode(1);
        paymentStatus.setAmount(3456);
        paymentStatus.setCurrency("EUR");
        paymentStatus.setOrder("ORDER");
        paymentStatus.setChallengeUrl("CHALLENGE_URL");
        var paymentTransaction = new PaymentTransaction(
                "127.0.1.1",
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION,
                "RANDOM_TPV_TOKEN",
                1,
                "RANDOM_ORDER_NAME",
                34.56,
                "20231205103259",
                PaymentTransactionState.PENDING,
                paymentStatus
        );
        return paymentTransaction;
    }

    public void assertHasSameData(PaymentTransaction savedPaymentTransaction, PaymentTransaction paymentTransaction) {
        PaymentStatus paymentStatus = paymentTransaction.getPaymentStatus();
        assertThat(savedPaymentTransaction).isNotNull();
        assertThat(savedPaymentTransaction.getIp()).isEqualTo(paymentTransaction.getIp());
        assertThat(savedPaymentTransaction.getPaymentMethod()).isEqualTo(paymentTransaction.getPaymentMethod());
        assertThat(savedPaymentTransaction.getTransactionType()).isEqualTo(paymentTransaction.getTransactionType());
        assertThat(savedPaymentTransaction.getTpvToken()).isEqualTo(paymentTransaction.getTpvToken());
        assertThat(savedPaymentTransaction.getTpvUser()).isEqualTo(paymentTransaction.getTpvUser());
        assertThat(savedPaymentTransaction.getOrder()).isEqualTo(paymentTransaction.getOrder());
        assertThat(savedPaymentTransaction.getAmount()).isEqualTo(paymentTransaction.getAmount());
        assertThat(savedPaymentTransaction.getDate()).isEqualTo(paymentTransaction.getDate());
        assertThat(savedPaymentTransaction.getTransactionState()).isEqualTo(paymentTransaction.getTransactionState());
        var savedPaymentStatus = savedPaymentTransaction.getPaymentStatus();
        assertThat(savedPaymentStatus.getOrder()).isEqualTo(paymentStatus.getOrder());
        assertThat(savedPaymentStatus.getAmount()).isEqualTo(paymentStatus.getAmount());
        assertThat(savedPaymentStatus.getChallengeUrl()).isEqualTo(paymentStatus.getChallengeUrl());
        assertThat(savedPaymentStatus.getCurrency()).isEqualTo(paymentStatus.getCurrency());
        assertThat(savedPaymentStatus.getErrorCode()).isEqualTo(paymentStatus.getErrorCode());
    }

    public PaymentTransactionBuilder id(int id) {
        item.setId(id);
        return this;
    }

    public PaymentTransactionBuilder state(PaymentTransactionState value) {
        item.setTransactionState(value);
        return this;
    }
}
