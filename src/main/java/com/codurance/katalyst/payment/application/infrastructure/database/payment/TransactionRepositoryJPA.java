package com.codurance.katalyst.payment.application.infrastructure.database.payment;

import com.codurance.katalyst.payment.application.model.payment.TransactionRepository;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionRepositoryJPA implements TransactionRepository {

    private final DBPaymentTransactionRepository jpaRepository;

    @Autowired
    public TransactionRepositoryJPA(DBPaymentTransactionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PaymentTransaction getPendingPaymentTransactionBasedOn(String orderCode) {
        var found = jpaRepository.findByOrderCodeAndTransactionState(
                orderCode,
                PaymentTransactionState.PENDING.getValue()
        );

        if (!found.isPresent()) {
            return null;
        }
        var dbPaymentTransaction = found.get();
        return dbPaymentTransaction.toPaymentTransaction();
    }

    @Override
    public PaymentTransaction save(PaymentTransaction paymentData) {
        var dbPaymentTransaction = new DBPaymentTransaction(paymentData);
        dbPaymentTransaction = jpaRepository.save(dbPaymentTransaction);
        return dbPaymentTransaction.toPaymentTransaction();
    }

    @Override
    public List<PaymentTransaction> getPaymentTransactionForRetry() {
        var paymentTransactions = jpaRepository.findAllByTransactionState(
                PaymentTransactionState.RETRY.getValue()
        );
        if (!paymentTransactions.isPresent()) {
            return null;
        }
        List<PaymentTransaction> result = new ArrayList<>();
        for (var dBPaymentTransaction : paymentTransactions.get()) {
            result.add(dBPaymentTransaction.toPaymentTransaction());
        }
        return result;
    }
}
