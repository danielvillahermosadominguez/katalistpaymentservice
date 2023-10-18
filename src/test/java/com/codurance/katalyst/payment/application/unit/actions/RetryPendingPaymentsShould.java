package com.codurance.katalyst.payment.application.unit.actions;

import com.codurance.katalyst.payment.application.actions.RetryPendingPayments;
import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.builders.PaymentTransactionBuilder;
import com.codurance.katalyst.payment.application.builders.PurchaseBuilder;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.financial.FinancialService;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NoCustomerData;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    void beforeEach() {
        int transactionId = 12345;
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
        retryPendingPayments.setActive(true);
        when(purchaseService.getPurchase(transactionId)).thenReturn(purchase);

    }

    @Test
    void not_to_do_anything_if_is_not_active() throws FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable, NoCustomerData {
        retryPendingPayments.setActive(false);

        retryPendingPayments.retry();

        verify(paymentService, never()).getRetryPayments();
        verify(purchaseService, never()).getPurchase(anyInt());
        verify(financialService, never()).emitInvoice(any());
        verify(learningService, never()).acquireACourseFor(any());
        verify(purchaseService, never()).updateFinantialStepFor(any(), anyBoolean());
        verify(purchaseService, never()).updateLearningStepFor(any(), anyBoolean());
    }

    @Test
    void not_to_do_anything_when_there_is_not_retry_payments() throws FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable, NoCustomerData {
        when(paymentService.getRetryPayments()).thenReturn(new ArrayList<>());

        retryPendingPayments.retry();

        verify(paymentService, times(1)).getRetryPayments();
        verify(purchaseService, never()).getPurchase(anyInt());
        verify(financialService, never()).emitInvoice(any());
        verify(learningService, never()).acquireACourseFor(any());
        verify(purchaseService, never()).updateFinantialStepFor(any(), anyBoolean());
        verify(purchaseService, never()).updateLearningStepFor(any(), anyBoolean());
    }

    @Test
    void not_process_retry_payments_which_dont_have_associated_purchase() throws FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable, NoCustomerData {
        var retryPayments = new ArrayList<PaymentTransaction>();
        var paymentTransactionBuilder = new PaymentTransactionBuilder();
        paymentTransactionBuilder
                .createWithDefaultValues()
                .state(PaymentTransactionState.RETRY)
                .id(1000);
        retryPayments.add(paymentTransactionBuilder.getItem());
        when(paymentService.getRetryPayments()).thenReturn(retryPayments);
        when(purchaseService.getPurchase(1000)).thenReturn(null);

        retryPendingPayments.retry();

        verify(paymentService, times(1)).getRetryPayments();
        verify(purchaseService, times(1)).getPurchase(1000);
        verify(financialService, never()).emitInvoice(any());
        verify(learningService, never()).acquireACourseFor(any());
        verify(purchaseService, never()).updateFinantialStepFor(any(), eq(true));
        verify(purchaseService, never()).updateLearningStepFor(any(), eq(true));
    }

    @Test
    void not_process_neither_financial_or_learning_when_financial_not_respond() throws FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable, NoCustomerData {
        var retryPayments = new ArrayList<PaymentTransaction>();
        var paymentTransactionBuilder = new PaymentTransactionBuilder();
        paymentTransactionBuilder
                .createWithDefaultValues()
                .state(PaymentTransactionState.RETRY)
                .id(1000);
        retryPayments.add(paymentTransactionBuilder.getItem());
        when(financialService.emitInvoice(any())).thenThrow(FinancialPlatformIsNotAvailable.class);
        when(paymentService.getRetryPayments()).thenReturn(retryPayments);
        var purchaseBuilder = new PurchaseBuilder();
        purchaseBuilder
                .createWithDefaultValues()
                .transactionId(1000)
                .financialStepOvercome(false)
                .learningStepOvercome(false);
        var purchase = purchaseBuilder.getItem();
        when(purchaseService.getPurchase(1000)).thenReturn(purchase);

        retryPendingPayments.retry();

        verify(paymentService, times(1)).getRetryPayments();
        verify(purchaseService, times(1)).getPurchase(1000);
        verify(financialService, times(1)).emitInvoice(eq(purchase));
        verify(learningService, never()).acquireACourseFor(any());
        verify(purchaseService, never()).updateFinantialStepFor(eq(purchase), anyBoolean());
        verify(purchaseService, never()).updateLearningStepFor(any(), anyBoolean());
        verify(paymentService, never()).updateTransaction(any());
    }

    @Test
    void process_financial_but_not_learning_when_learning_not_respond() throws FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable, NoCustomerData {
        var retryPayments = new ArrayList<PaymentTransaction>();
        var paymentTransactionBuilder = new PaymentTransactionBuilder();
        paymentTransactionBuilder
                .createWithDefaultValues()
                .state(PaymentTransactionState.RETRY)
                .id(1000);
        retryPayments.add(paymentTransactionBuilder.getItem());
        when(financialService.emitInvoice(any())).thenReturn(true);
        when(learningService.acquireACourseFor(any())).thenThrow(LearningPlatformIsNotAvailable.class);
        when(paymentService.getRetryPayments()).thenReturn(retryPayments);
        var purchaseBuilder = new PurchaseBuilder();
        purchaseBuilder
                .createWithDefaultValues()
                .transactionId(1000)
                .financialStepOvercome(false)
                .learningStepOvercome(false);
        var purchase = purchaseBuilder.getItem();
        when(purchaseService.getPurchase(1000)).thenReturn(purchase);
        when(purchaseService.updateFinantialStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setFinantialStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });

        retryPendingPayments.retry();

        verify(paymentService, times(1)).getRetryPayments();
        verify(purchaseService, times(1)).getPurchase(1000);
        verify(financialService, times(1)).emitInvoice(eq(purchase));
        verify(learningService, times(1)).acquireACourseFor(eq(purchase));
        verify(purchaseService, times(1)).updateFinantialStepFor(eq(purchase), eq(true));
        verify(purchaseService, never()).updateLearningStepFor(any(), anyBoolean());
        verify(paymentService, never()).updateTransaction(any());
    }

    @Test
    void process_retry_payments_which_dint_process_the_financial_step() throws FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable, NoCustomerData {
        var paymentTransactionCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        var retryPayments = new ArrayList<PaymentTransaction>();
        var paymentTransactionBuilder = new PaymentTransactionBuilder();
        paymentTransactionBuilder
                .createWithDefaultValues()
                .state(PaymentTransactionState.RETRY)
                .id(1000);
        retryPayments.add(paymentTransactionBuilder.getItem());
        when(paymentService.getRetryPayments()).thenReturn(retryPayments);
        var purchaseBuilder = new PurchaseBuilder();
        purchaseBuilder
                .createWithDefaultValues()
                .transactionId(1000)
                .financialStepOvercome(false)
                .learningStepOvercome(true);

        var purchase = purchaseBuilder.getItem();
        when(purchaseService.getPurchase(1000)).thenReturn(purchase);
        when(financialService.emitInvoice(any())).thenReturn(true);
        when(purchaseService.updateFinantialStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setFinantialStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });

        retryPendingPayments.retry();

        verify(paymentService, times(1)).getRetryPayments();
        verify(purchaseService, times(1)).getPurchase(1000);
        verify(financialService, times(1)).emitInvoice(purchase);
        verify(learningService, never()).acquireACourseFor(any());
        verify(purchaseService, times(1)).updateFinantialStepFor(eq(purchase), eq(true));
        verify(purchaseService, never()).updateLearningStepFor(any(), eq(true));
        verify(paymentService).updateTransaction(paymentTransactionCaptor.capture());
        var updatedPaymentTransaction = paymentTransactionCaptor.getValue();
        assertThat(updatedPaymentTransaction.getTransactionState()).isEqualTo(PaymentTransactionState.DONE);
    }

    @Test
    void process_retry_payments_which_dint_process_the_learning_step() throws FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable, NoCustomerData {
        var paymentTransactionCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        var retryPayments = new ArrayList<PaymentTransaction>();
        var paymentTransactionBuilder = new PaymentTransactionBuilder();
        paymentTransactionBuilder
                .createWithDefaultValues()
                .state(PaymentTransactionState.RETRY)
                .id(1000);
        retryPayments.add(paymentTransactionBuilder.getItem());
        when(paymentService.getRetryPayments()).thenReturn(retryPayments);
        var purchaseBuilder = new PurchaseBuilder();
        purchaseBuilder
                .createWithDefaultValues()
                .transactionId(1000)
                .financialStepOvercome(true)
                .learningStepOvercome(false);

        var purchase = purchaseBuilder.getItem();
        when(purchaseService.getPurchase(1000)).thenReturn(purchase);
        when(learningService.acquireACourseFor(any())).thenReturn(true);
        when(purchaseService.updateLearningStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setLearningStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });

        retryPendingPayments.retry();

        verify(paymentService, times(1)).getRetryPayments();
        verify(purchaseService, times(1)).getPurchase(1000);
        verify(financialService, never()).emitInvoice(any());
        verify(learningService, times(1)).acquireACourseFor(eq(purchase));
        verify(purchaseService, never()).updateFinantialStepFor(any(), eq(true));
        verify(purchaseService, times(1)).updateLearningStepFor(eq(purchase), eq(true));
        verify(paymentService).updateTransaction(paymentTransactionCaptor.capture());
        var updatedPaymentTransaction = paymentTransactionCaptor.getValue();
        assertThat(updatedPaymentTransaction.getTransactionState()).isEqualTo(PaymentTransactionState.DONE);
    }

    @Test
    void process_retry_payments_which_dint_process_financial_and_learning_step() throws FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable, NoCustomerData {
        var paymentTransactionCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        var retryPayments = new ArrayList<PaymentTransaction>();
        var paymentTransactionBuilder = new PaymentTransactionBuilder();
        paymentTransactionBuilder
                .createWithDefaultValues()
                .state(PaymentTransactionState.RETRY)
                .id(1000);
        retryPayments.add(paymentTransactionBuilder.getItem());
        when(financialService.emitInvoice(any())).thenReturn(true);
        when(paymentService.getRetryPayments()).thenReturn(retryPayments);
        var purchaseBuilder = new PurchaseBuilder();
        purchaseBuilder
                .createWithDefaultValues()
                .transactionId(1000)
                .financialStepOvercome(false)
                .learningStepOvercome(false);

        var purchase = purchaseBuilder.getItem();
        when(purchaseService.getPurchase(1000)).thenReturn(purchase);
        when(learningService.acquireACourseFor(any())).thenReturn(true);
        when(purchaseService.updateFinantialStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setFinantialStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });
        when(purchaseService.updateLearningStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setLearningStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });

        retryPendingPayments.retry();

        verify(paymentService, times(1)).getRetryPayments();
        verify(purchaseService, times(1)).getPurchase(1000);
        verify(financialService, times(1)).emitInvoice(eq(purchase));
        verify(learningService, times(1)).acquireACourseFor(eq(purchase));
        verify(purchaseService, times(1)).updateFinantialStepFor(eq(purchase), eq(true));
        verify(purchaseService, times(1)).updateLearningStepFor(eq(purchase), eq(true));
        verify(paymentService).updateTransaction(paymentTransactionCaptor.capture());
        var updatedPaymentTransaction = paymentTransactionCaptor.getValue();
        assertThat(updatedPaymentTransaction.getTransactionState()).isEqualTo(PaymentTransactionState.DONE);
    }
}
