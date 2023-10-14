package com.codurance.katalyst.payment.application.model.payment;

import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;

public interface TransactionRepository {
    PaymentTransaction getPendingPaymentTransactionBasedOn(String orderName);

    PaymentTransaction save(PaymentTransaction paymentData);
}
