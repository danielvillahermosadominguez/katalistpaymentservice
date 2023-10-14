package com.codurance.katalyst.payment.application.infrastructure.database.purchase;

import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseRepository;
import org.springframework.stereotype.Service;

@Service
public class PurchaseRepositoryJPA implements PurchaseRepository {
    private final DBPurchaseRepository jpaRepository;

    public PurchaseRepositoryJPA(DBPurchaseRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Purchase save(Purchase purchase) {
        var dbPurchase = new DBPurchase(purchase);
        dbPurchase = jpaRepository.save(dbPurchase);
        return dbPurchase.toPurchase();
    }

    @Override
    public Purchase findPurchaseByTransactionId(int transactionId) {
        var result = jpaRepository.findByTransactionId(transactionId);
        if (!result.isPresent()) {
            return null;
        }
        var dbPurchase = result.get();
        return dbPurchase.toPurchase();
    }

    @Override
    public Purchase findPurchaseById(int id) {
        var result = jpaRepository.findById(id);
        if (!result.isPresent()) {
            return null;
        }
        var dbPurchase = result.get();
        return dbPurchase.toPurchase();
    }
}
