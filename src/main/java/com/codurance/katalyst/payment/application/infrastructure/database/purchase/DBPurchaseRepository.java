package com.codurance.katalyst.payment.application.infrastructure.database.purchase;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DBPurchaseRepository extends CrudRepository<DBPurchase, Integer> {
    Optional<DBPurchase> findByTransactionId(int transactionId);
}