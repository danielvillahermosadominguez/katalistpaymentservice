package com.codurance.katalyst.payment.application.infrastructure.database.payment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DBPaymentTransactionRepository extends CrudRepository<DBPaymentTransaction, Long> {
    Optional<DBPaymentTransaction> findByOrderCodeAndTransactionState(String orderCode, String transactionState);

    Optional<Iterable<DBPaymentTransaction>> findAllByTransactionState(String transactionState);

    Optional<Iterable<DBPaymentTransaction>> findAllByOrderCode(String orderCode);
}
