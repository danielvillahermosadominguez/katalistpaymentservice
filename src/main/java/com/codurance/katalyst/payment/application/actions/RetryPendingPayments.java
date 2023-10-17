package com.codurance.katalyst.payment.application.actions;

import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.financial.FinancialService;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RetryPendingPayments {
    private final PurchaseService purchaseService;
    private final FinancialService financialService;
    private final LearningService learningService;
    private final AbstractLog log;
    private PaymentService paymentService;

    @Value("${payments.retry.active:false}")
    private boolean active;

    @Autowired
    public RetryPendingPayments(PaymentService paymentService,
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
    @Scheduled(initialDelayString = "${payments.retry.initialDelay:5000}", fixedRateString = "${payments.retry.fixedRate:2500}")
    public void retry() {
        if(active) {
            log.warn(ConfirmPayment.class, "[RETRY] -----------------TEST-------------------------------");
        }
    }
}
