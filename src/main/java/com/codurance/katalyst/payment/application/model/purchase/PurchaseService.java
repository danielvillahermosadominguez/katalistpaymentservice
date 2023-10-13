package com.codurance.katalyst.payment.application.model.purchase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    @Autowired
    public PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public Purchase getPurchase(int transactionId) {
        return purchaseRepository.findPurchaseByTransactionId(transactionId);
    }

    public void updateFinantialStepFor(Purchase purchase, boolean stepOvercome) {
        throw new UnsupportedOperationException();
    }

    public void updateLearningStepFor(Purchase purchase, boolean septOvercome) {
        throw new UnsupportedOperationException();
    }

    public Purchase save(Purchase purchase) {
        return purchaseRepository.save(purchase);
    }
}
