package com.codurance.katalyst.payment.application.unit.actions;

import com.codurance.katalyst.payment.application.actions.ConfirmPayment;
import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.builders.PaymentNotificationBuilder;
import com.codurance.katalyst.payment.application.builders.PaymentTransactionBuilder;
import com.codurance.katalyst.payment.application.builders.PurchaseBuilder;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.financial.FinancialService;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NoCustomerData;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.payment.exceptions.PaymentTransactionNotFound;
import com.codurance.katalyst.payment.application.model.ports.email.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfirmPaymentShould {
    public static final int NOT_VALID_TRANSACTION = 4453;
    private PaymentNotification notification;
    private PaymentTransaction paymentTransaction;
    private PaymentService paymentService;
    private PurchaseService purchaseService;
    private FinancialService financialService;
    private LearningService learningService;
    private ConfirmPayment confirmPayment;
    private Purchase purchase;
    private AbstractLog log;

    @BeforeEach
    void beforeEach() throws NotValidNotification, PaymentTransactionNotFound {
        int transactionId = 12345;
        var builder = new PaymentNotificationBuilder();
        notification = builder
                .createPaymentNotificationByDefault()
                .getItem();
        var paymentTransactionBuilder = new PaymentTransactionBuilder();
        paymentTransaction = paymentTransactionBuilder.createWithDefaultValues()
                .id(transactionId)
                .getItem();
        purchase = createPurchaseFixture(transactionId);
        paymentService = mock(PaymentService.class);
        purchaseService = mock(PurchaseService.class);
        financialService = mock(FinancialService.class);
        learningService = mock(LearningService.class);
        log = mock(AbstractLog.class);
        confirmPayment = new ConfirmPayment(
                paymentService,
                purchaseService,
                financialService,
                learningService,
                log
        );
        when(paymentService.confirmPayment(notification)).thenReturn(paymentTransaction);
        when(purchaseService.getPurchase(transactionId)).thenReturn(purchase);
    }

    @Test
    void confirm_the_payment() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable, PaymentTransactionNotFound {
        confirmPayment.confirm(notification);

        verify(paymentService, times(1)).confirmPayment(any());
    }

    @Test
    void ignore_payment_notification_not_pending() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable, PaymentTransactionNotFound {
        when(paymentService.confirmPayment(any())).thenThrow(PaymentTransactionNotFound.class);

        confirmPayment.confirm(notification);

        verify(paymentService, times(1)).confirmPayment(any());
        verify(purchaseService, never()).getPurchase(anyInt());
        verify(purchaseService, never()).updateLearningStepFor(any(),anyBoolean());
        verify(purchaseService, never()).updateFinantialStepFor(any(),anyBoolean());
        verify(financialService, never()).emitInvoice(any());
        verify(learningService, never()).acquireACourseFor(any());
    }

    @Test
    void throw_not_valid_notification_when_payment_service_detect_is_not_valid() throws NotValidNotification, PaymentTransactionNotFound {
        when(paymentService.confirmPayment(notification)).thenThrow(NotValidNotification.class);
        var exception = assertThrows(NotValidNotification.class, () -> {
            confirmPayment.confirm(notification);
        });

        assertThat(exception).isNotNull();
    }

    @Test
    void obtain_the_customer_related_to_the_payment_transaction() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        var idTransaction = paymentTransaction.getId();
        confirmPayment.confirm(notification);

        verify(purchaseService, times(1)).getPurchase(idTransaction);
    }

    @Test
    void throw_exception_no_customer_data_related_to_this_transaction() {
        this.paymentTransaction.setId(NOT_VALID_TRANSACTION);
        var exception = assertThrows(NoCustomerData.class, () -> {
            confirmPayment.confirm(notification);
        });
        assertThat(exception).isNotNull();

    }

    @Test
    void emit_an_invoice_from_the_financial_service() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        when(financialService.emitInvoice(any())).thenReturn(true);

        confirmPayment.confirm(notification);

        verify(financialService, times(1)).emitInvoice(purchase);
        verify(purchaseService, times(1)).updateFinantialStepFor(purchase, true);
    }

    @Test
    void update_purchase_financial_step_not_passed() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        when(financialService.emitInvoice(any())).thenReturn(false);

        confirmPayment.confirm(notification);

        verify(financialService, times(1)).emitInvoice(purchase);
        verify(purchaseService, never()).updateFinantialStepFor(any(), anyBoolean());
    }

    @Test
    void acquire_a_course_with_the_purchase() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        when(learningService.acquireACourseFor(any())).thenReturn(true);

        confirmPayment.confirm(notification);

        verify(learningService, times(1)).acquireACourseFor(purchase);
        verify(purchaseService, times(1)).updateLearningStepFor(purchase, true);
    }

    @Test
    void update_purchase_learning_step_not_passed() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        when(learningService.acquireACourseFor(any())).thenReturn(false);

        confirmPayment.confirm(notification);

        verify(learningService, times(1)).acquireACourseFor(purchase);
        verify(purchaseService, never()).updateLearningStepFor(any(), anyBoolean());
    }

    @Test
    void update_transaction_to_done_when_purchase_and_financial_step_are_done() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        var paymentTransactionCapture = ArgumentCaptor.forClass(PaymentTransaction.class);
        when(learningService.acquireACourseFor(any())).thenReturn(true);
        when(financialService.emitInvoice(any())).thenReturn(true);
        when(purchaseService.updateFinantialStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setFinantialStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });
        when(purchaseService.updateLearningStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setLearningStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });

        confirmPayment.confirm(notification);

        verify(paymentService, times(1)).updateTransaction(
                paymentTransactionCapture.capture()
        );
        var updatedTransaction = paymentTransactionCapture.getValue();
        assertThat(updatedTransaction.getTransactionState()).isEqualTo(PaymentTransactionState.DONE);

    }

    @Test
    void update_transaction_to_retry_when_learning_platform_is_not_available() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        var paymentTransactionCapture = ArgumentCaptor.forClass(PaymentTransaction.class);
        when(learningService.acquireACourseFor(any())).thenThrow(LearningPlatformIsNotAvailable.class);
        when(financialService.emitInvoice(any())).thenReturn(true);
        when(purchaseService.updateFinantialStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setFinantialStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });
        when(purchaseService.updateLearningStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setLearningStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });

        confirmPayment.confirm(notification);

        verify(paymentService, times(1)).updateTransaction(
                paymentTransactionCapture.capture()
        );
        var updatedTransaction = paymentTransactionCapture.getValue();
        assertThat(updatedTransaction.getTransactionState()).isEqualTo(PaymentTransactionState.RETRY);

    }

    @Test
    void update_transaction_to_retry_when_financial_platform_is_not_available() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        var paymentTransactionCapture = ArgumentCaptor.forClass(PaymentTransaction.class);
        when(learningService.acquireACourseFor(any())).thenReturn(true);
        when(financialService.emitInvoice(any())).thenThrow(FinancialPlatformIsNotAvailable.class);
        when(purchaseService.updateFinantialStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setFinantialStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });
        when(purchaseService.updateLearningStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setLearningStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });

        confirmPayment.confirm(notification);

        verify(paymentService, times(1)).updateTransaction(
                paymentTransactionCapture.capture()
        );
        var updatedTransaction = paymentTransactionCapture.getValue();
        assertThat(updatedTransaction.getTransactionState()).isEqualTo(PaymentTransactionState.RETRY);

    }

    @Test
    void update_transaction_to_retry_when_the_purchase_has_any_not_valid_email_format() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        var paymentTransactionCapture = ArgumentCaptor.forClass(PaymentTransaction.class);
        when(learningService.acquireACourseFor(any())).thenReturn(true);
        when(financialService.emitInvoice(any())).thenThrow(NotValidEMailFormat.class);
        when(purchaseService.updateFinantialStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setFinantialStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });
        when(purchaseService.updateLearningStepFor(any(), anyBoolean())).thenAnswer(invocation -> {
            purchase.setLearningStepOvercome(invocation.getArgument(1));
            return invocation.getArgument(0);
        });

        confirmPayment.confirm(notification);

        verify(paymentService, times(1)).updateTransaction(
                paymentTransactionCapture.capture()
        );
        var updatedTransaction = paymentTransactionCapture.getValue();
        assertThat(updatedTransaction.getTransactionState()).isEqualTo(PaymentTransactionState.RETRY);

    }

    private Purchase createPurchaseFixture(int transactionId) {
        var purchaseBuilder = new PurchaseBuilder();

        return purchaseBuilder
                .createWithDefaultValues()
                .transactionId(transactionId)
                .order("RANDOM_ORDER_NAME")
                .getItem();
    }
}
