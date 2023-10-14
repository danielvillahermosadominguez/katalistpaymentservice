package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class PurchaseRepositoryFake implements PurchaseRepository {

    int purchaseId = 0;
    private List<Purchase> purchases = new ArrayList<>();

    public void reset() {
        purchases.clear();
    }

    @Override
    public Purchase save(Purchase purchase) {
        var oldPurchase = findPurchaseById(purchase.getId());
        if (oldPurchase == null) {
            purchaseId++;
            purchase.setId(purchaseId);
            purchases.add(purchase);
            return purchase;
        }
        return update(purchase);
    }

    @Override
    public Purchase findPurchaseByTransactionId(int transactionId) {
        var list = purchases.stream()
                .filter(p -> p.getTransactionId() == transactionId)
                .collect(Collectors.toList());
        if (list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            throw new RuntimeException("ERROR: you have several purchases with the same transaction id");
        }
        return list.get(0);
    }

    private Purchase update(Purchase purchase) {
        var oldPurchase = findPurchaseById(purchase.getId());
        if (oldPurchase == null) {
            throw new RuntimeException("ERROR: is there not a purchase with id = " + purchase.getId());
        }
        purchases.remove(oldPurchase);
        purchases.add(purchase);
        return purchase;
    }

    @Override
    public Purchase findPurchaseById(int id) {
        var list = purchases.stream()
                .filter(p -> p.getId() == id)
                .collect(Collectors.toList());
        if (list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            throw new RuntimeException("ERROR: you have several purchases with the same id");
        }
        return list.get(0);
    }
}