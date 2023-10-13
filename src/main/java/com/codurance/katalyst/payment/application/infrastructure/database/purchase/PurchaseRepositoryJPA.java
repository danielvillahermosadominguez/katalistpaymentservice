package com.codurance.katalyst.payment.application.infrastructure.database.purchase;

import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseRepository;
import org.springframework.stereotype.Component;

@Component
public class PurchaseRepositoryJPA implements PurchaseRepository {
    @Override
    public Purchase save(Purchase purchase) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Purchase findPurchaseByTransactionId(int transactionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Purchase update(Purchase capture) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Purchase findPurchaseById(int id) {
        throw new UnsupportedOperationException();
    }
}
