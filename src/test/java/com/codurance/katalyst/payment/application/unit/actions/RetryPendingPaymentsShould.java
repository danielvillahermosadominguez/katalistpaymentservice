package com.codurance.katalyst.payment.application.unit.actions;

import com.codurance.katalyst.payment.application.actions.ConfirmPayment;
import com.codurance.katalyst.payment.application.actions.RetryPendingPayments;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.financial.FinancialService;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.payment.exceptions.PaymentTransactionNotFound;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetryPendingPaymentsShould {
    private PaymentTransaction paymentTransaction;
    private PaymentService paymentService;
    private PurchaseService purchaseService;
    private FinancialService financialService;
    private LearningService learningService;
    private RetryPendingPayments retryPendingPayments;
    private Purchase purchase;
    private AbstractLog log;

    @BeforeEach
    void beforeEach() throws NotValidNotification, PaymentTransactionNotFound {
        int transactionId = 12345;
        //paymentTransaction = createPaymentTransactionFixture(transactionId);
        //purchase = createPurchaseFixture(transactionId);
        paymentService = mock(PaymentService.class);
        purchaseService = mock(PurchaseService.class);
        financialService = mock(FinancialService.class);
        learningService = mock(LearningService.class);
        log = mock(AbstractLog.class);
        retryPendingPayments = new RetryPendingPayments(
                paymentService,
                purchaseService,
                financialService,
                learningService,
                log
        );
        when(purchaseService.getPurchase(transactionId)).thenReturn(purchase);
    }
    @Test
    void not_to_do_anything_if_is_not_active() {

    }
}
