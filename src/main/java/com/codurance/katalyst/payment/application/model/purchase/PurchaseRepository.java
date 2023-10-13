package com.codurance.katalyst.payment.application.model.purchase;

public interface PurchaseRepository {
    Purchase save(Purchase purchase);

    Purchase findPurchaseByTransactionId(int transactionId);
}
