package com.codurance.katalyst.payment.application.actions;

import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.financial.FinancialService;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NoCustomerData;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.payment.exceptions.PaymentTransactionNotFound;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ConfirmPayment {
    private final PurchaseService purchaseService;
    private final FinancialService financialService;
    private final LearningService learningService;
    private final AbstractLog log;
    private PaymentService paymentService;

    @Autowired
    public ConfirmPayment(PaymentService paymentService,
                          PurchaseService purchaseService,
                          FinancialService financialService,
                          LearningService learningService,
                          AbstractLog log
    ) {
        this.paymentService = paymentService;
        this.purchaseService = purchaseService;
        this.financialService = financialService;
        this.learningService = learningService;
        this.log = log;
    }

    public void confirm(PaymentNotification notification) throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        PaymentTransaction paymentTransaction = null;

        try {
            paymentTransaction = paymentService.confirmPayment(notification);
        } catch (PaymentTransactionNotFound exception) {
            logIgnoredNotification(notification);
            return;
        }
        var purchase = purchaseService.getPurchase(paymentTransaction.getId());
        if (purchase == null) {
            log.error(ConfirmPayment.class, String.format("[NOT PURCHASE DATA AVAILABLE]: for transaction id = %s", paymentTransaction.getId()));
            throw new NoCustomerData();
        }
        try {
            if (financialService.emitInvoice(purchase)) {
                purchaseService.updateFinantialStepFor(purchase, true);
            }

            if (learningService.acquireACourseFor(purchase)) {
                purchaseService.updateLearningStepFor(purchase, true);
            }
        } catch (Exception exception) {
            logNotProcessablePurchase(notification);
        }
    }

    private void logNotProcessablePurchase(PaymentNotification notification) {
        var objectMapper = new ObjectMapper();
        try {
            var json = objectMapper.writeValueAsString(notification);
            log.error(ConfirmPayment.class,
                    String.format("[PURCHASE NOT PROCESSABLE]:Not possible to emit the invoice or/and acquire the course for the purchase %s",
                            json)
            );
        } catch (JsonProcessingException e) {
            log.error(
                    ConfirmPayment.class,
                    String.format("[PURCHASE NOT PROCESSABLE]:Not possible to emit the invoice or/and acquire the course for the purchase. Purchase cannot be converted into json format: ",
                            e.getMessage())
            );
        }
    }

    private void logIgnoredNotification(PaymentNotification notification) {
        var objectMapper = new ObjectMapper();
        try {
            var json = objectMapper.writeValueAsString(notification);
            log.warn(ConfirmPayment.class, String.format("[IGNORED PAYMENT NOTIFICATION]: %s", json));
        } catch (JsonProcessingException e) {
            log.warn(ConfirmPayment.class, String.format("[IGNORED PAYMENT NOTIFICATION]: No data available because: %s", e.getMessage()));
        }
    }
}