package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseRepository;

import java.util.HashMap;
import java.util.Map;


public class PurchaseRepositoryFake implements PurchaseRepository {

    int purchaseId = 0;
    private Map<Integer, Purchase> purchases = new HashMap<>();

    public void reset() {
        purchases.clear();
    }

    @Override
    public Purchase save(Purchase purchase) {
        purchaseId++;
        purchase.setId(purchaseId);
        purchases.put(purchase.getTransactionId(), purchase);
        return purchase;
    }

    @Override
    public Purchase findPurchaseByTransactionId(int transactionId) {
        if (purchases.containsKey(transactionId)) {
            return purchases.get(transactionId);
        }
        return null;
    }
}