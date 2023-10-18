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
        active = value;
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
            var retryPayments = paymentService.getRetryPayments();
            log.warn(ConfirmPayment.class, String.format("[RETRY TRANSACTION PAYMENTS] %s transactions to process", retryPayments.toArray().length));
            if (retryPayments.isEmpty()) {
                return;
            }

            for (var paymentTransaction : retryPayments) {
                log.warn(ConfirmPayment.class, String.format("[RETRY TRANSACTION PAYMENTS] Start to process transaction = %s", paymentTransaction.getId()));
                var purchase = purchaseService.getPurchase(paymentTransaction.getId());
                if (purchase == null) {
                    log.error(ConfirmPayment.class, String.format("[RETRY TRANSACTION PAYMENTS]: Not purchase data available for transaction id = %s", paymentTransaction.getId()));
                    continue;
                }
                try {
                    if (!purchase.isProcessedInFinantialState()) {
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
                    log.error(ConfirmPayment.class, String.format("[RETRY TRANSACTION PAYMENTS]: There was a generic exception for transaction %s: %s", paymentTransaction.getId(), exception.getMessage()));
                } catch (LearningPlatformIsNotAvailable e) {
                    log.error(ConfirmPayment.class, String.format("[RETRY TRANSACTION PAYMENTS]: Learning platform is not available for transaction %s", paymentTransaction.getId()));
                } catch (FinancialPlatformIsNotAvailable e) {
                    log.error(ConfirmPayment.class, String.format("[RETRY TRANSACTION PAYMENTS]: Financial platform is not available for transaction %s", paymentTransaction.getId()));
                } catch (InvalidInputCustomerData e) {
                    log.error(ConfirmPayment.class, String.format("[RETRY TRANSACTION PAYMENTS]: Some problems with the purchase data for transaction %s: %s", paymentTransaction.getId(), e.getMessage()));
                }

                if(purchase.isProcessedInLearningState() && purchase.isProcessedInFinantialState()){
                    paymentTransaction.setTransactionState(PaymentTransactionState.DONE);
                    paymentService.updateTransaction(paymentTransaction);
                    log.error(ConfirmPayment.class, String.format("[RETRY TRANSACTION PAYMENTS]: Transaction %s to state DONE", paymentTransaction.getId()));
                }
            }
        }
    }
}
