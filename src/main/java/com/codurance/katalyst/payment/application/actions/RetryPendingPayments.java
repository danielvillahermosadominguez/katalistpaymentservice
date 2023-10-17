package com.codurance.katalyst.payment.application.actions;

import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.financial.FinancialService;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NoCustomerData;
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

    public void setActive(boolean value) {
        active = true;
    }
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
    public void retry() throws NoCustomerData {
        if(active) {
            var retryPayments = paymentService.getRetryPaiments();
            log.warn(ConfirmPayment.class, "[RETRY] -----------------TEST-------------------------------");
            if(retryPayments.isEmpty()) {
                return;
            }

            log.warn(ConfirmPayment.class, "Processing retry payments");
            for (var paymentTransaction: retryPayments) {
                var purchase = purchaseService.getPurchase(paymentTransaction.getId());
                if (purchase == null) {
                    log.error(ConfirmPayment.class, String.format("[NOT PURCHASE DATA AVAILABLE]: for transaction id = %s", paymentTransaction.getId()));
                    throw new NoCustomerData();
                }
                try {
                    if(!purchase.isProcessedInFinantialState()) {
                        if (financialService.emitInvoice(purchase)) {
                            purchase = purchaseService.updateFinantialStepFor(purchase, true);
                        }
                    }

                    if(!purchase.isProcessedInLearningState()) {
                        if (learningService.acquireACourseFor(purchase)) {
                            purchase = purchaseService.updateLearningStepFor(purchase, true);
                        }
                    }
                } catch (Exception exception) {
                    //TODO: Put a log and we put in false Special alert
                } catch (LearningPlatformIsNotAvailable e) {
                    //TODO: Put a log and we put in false
                } catch (FinancialPlatformIsNotAvailable e) {
                    //TODO: Put a log and we put in false
                } catch (InvalidInputCustomerData e) {
                    //TODO: Put a log and we put in false Special alert
                }
                if(purchase.isProcessedInLearningState() && purchase.isProcessedInFinantialState()){
                    paymentTransaction.setTransactionState(PaymentTransactionState.DONE);
                    paymentService.updateTransaction(paymentTransaction);
                }
            }
        }
    }
}
