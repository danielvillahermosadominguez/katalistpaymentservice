package com.codurance.katalyst.payment.application.infrastructure.database.payment;

import com.codurance.katalyst.payment.application.model.payment.TransactionRepository;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
