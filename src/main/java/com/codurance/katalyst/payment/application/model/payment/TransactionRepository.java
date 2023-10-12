package com.codurance.katalyst.payment.application.model.payment;

import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;

public interface TransactionRepository {
    PaymentTransaction getOpenTransactionBasedOn(String orderName);
}
