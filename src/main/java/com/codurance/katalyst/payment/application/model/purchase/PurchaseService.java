package com.codurance.katalyst.payment.application.model.purchase;

import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import org.springframework.stereotype.Service;

@Service
public class PurchaseService {
    public Purchase getPurchase(int transactionId) {
        throw new UnsupportedOperationException();
    }

    public void updateFinantialStepFor(Purchase purchase, boolean stepOvercome) {
        throw new UnsupportedOperationException();
    }

    public void updateLearningStepFor(Purchase purchase, boolean setpOvercome) {
        throw new UnsupportedOperationException();
    }
}
