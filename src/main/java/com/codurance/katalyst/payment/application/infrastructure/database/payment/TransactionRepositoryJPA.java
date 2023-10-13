package com.codurance.katalyst.payment.application.infrastructure.database.payment;

import com.codurance.katalyst.payment.application.model.payment.TransactionRepository;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
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
    public PaymentTransaction getOpenTransactionBasedOn(String orderCode) {
        var found = jpaRepository.findByOrderCode(orderCode);
        if (!found.isPresent()) {
            return null;
        }
        var dbPaymentTransaction = found.get();
        return dbPaymentTransaction.toPaymentTransaction();
    }

    @Override
    public PaymentTransaction save(PaymentTransaction paymentData) {
        DBPaymentTransaction dbPaymentTransaction = null;
        var found = jpaRepository.findById((long) paymentData.getId());
        if (found.isPresent()) {
            dbPaymentTransaction = found.get();
            dbPaymentTransaction.update(paymentData);
        } else {
            dbPaymentTransaction = new DBPaymentTransaction(paymentData);
        }
        dbPaymentTransaction = jpaRepository.save(dbPaymentTransaction);
        paymentData.setId(dbPaymentTransaction.getId());
        return paymentData;
    }
}
