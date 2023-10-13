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

    public Purchase updateFinantialStepFor(Purchase purchase, boolean stepOvercome) {
        var originalPurchase = purchaseRepository.findPurchaseById(purchase.getId());
        originalPurchase.setFinantialState(stepOvercome);
        purchaseRepository.update(purchase);
        return originalPurchase;
    }

    public Purchase updateLearningStepFor(Purchase purchase, boolean septOvercome) {
        var originalPurchase = purchaseRepository.findPurchaseById(purchase.getId());
        originalPurchase.setLearningState(septOvercome);
        purchaseRepository.update(purchase);
        return originalPurchase;
    }

    public Purchase save(Purchase purchase) {
        return purchaseRepository.save(purchase);
    }
}
