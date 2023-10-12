package com.codurance.katalyst.payment.application.infrastructure.database;

import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.TransactionRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepositoryJPA  implements TransactionRepository {

    @Override
    public PaymentTransaction getOpenTransactionBasedOn(String orderName) {
        throw new UnsupportedOperationException();
    }
}
