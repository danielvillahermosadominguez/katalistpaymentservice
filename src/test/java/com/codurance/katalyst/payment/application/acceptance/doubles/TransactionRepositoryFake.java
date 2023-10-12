package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.model.payment.TransactionRepository;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;

import java.util.HashMap;
import java.util.Map;

public class TransactionRepositoryFake implements TransactionRepository {

    private Map<String, PaymentTransaction> paymenTransactions = new HashMap<>();

    @Override
    public PaymentTransaction getOpenTransactionBasedOn(String orderName) {
        if (!paymenTransactions.containsKey(orderName)) {
            return null;
        }
        return paymenTransactions.get(orderName);
    }

    @Override
    public void save(PaymentTransaction paymentTransaction) {
        paymenTransactions.put(paymentTransaction.getOrder(), paymentTransaction);
    }

    public void reset() {
        paymenTransactions.clear();
        ;
    }
}
