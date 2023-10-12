package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.model.payment.TransactionRepository;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;

public class TransactionRepositoryFake implements TransactionRepository {

    @Override
    public PaymentTransaction getOpenTransactionBasedOn(String orderName) {
        return null;
    }
}
