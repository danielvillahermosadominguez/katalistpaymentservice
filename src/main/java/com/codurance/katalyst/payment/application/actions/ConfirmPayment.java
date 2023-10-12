package com.codurance.katalyst.payment.application.actions;

import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.financial.FinancialService;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NoCustomerData;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfirmPayment {
    private final PurchaseService purchaseService;
    private final FinancialService financialService;
    private final LearningService learningService;
    private PaymentService paymentService;
    private int terminal;

    @Autowired
    public ConfirmPayment(PaymentService paymentService,
                          PurchaseService purchaseService,
                          FinancialService financialService,
                          LearningService learningService,
                          @Value("${paycomet.terminal}") int terminal
    ) {
        this.paymentService = paymentService;
        this.purchaseService = purchaseService;
        this.terminal = terminal;
        this.financialService = financialService;
        this.learningService = learningService;
    }

    public void confirm(PaymentNotification notification) throws NotValidNotification, NoCustomerData {
        checkValidNotification(notification);
        var paymentTransaction = paymentService.confirmPayment(notification);
        var purchase = purchaseService.getPurchase(paymentTransaction.getId());
        if (purchase == null) {
            throw new NoCustomerData();
        }

        if (financialService.emitInvoice(purchase)) {
            purchaseService.updateFinantialStepFor(purchase, true);
        }

        if(learningService.acquireACourseFor(purchase)) {
            purchaseService.updateLearningStepFor(purchase, true);
        }

    }

    private void checkValidNotification(PaymentNotification notification) throws NotValidNotification {
        if (
                notification.getTransactionType() != TransactionType.AUTHORIZATION |
                        notification.getMethodId() != PaymentMethod.CARDS |
                        notification.getTpvID() != terminal
        ) {
            throw new NotValidNotification();
        }
    }
}