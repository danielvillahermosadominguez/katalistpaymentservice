package com.codurance.katalyst.payment.application.unit.actions;

import com.codurance.katalyst.payment.application.actions.ConfirmPayment;
import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.model.financial.FinancialService;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NoCustomerData;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

    @BeforeEach
    void beforeEach() throws NotValidNotification {
        int transactionId = 12345;
        notification = createNotificationFixture();
        paymentTransaction = createPaymentTransactionFixture(transactionId);
        purchase = createPurchaseFixture(transactionId);
        paymentService = mock(PaymentService.class);
        purchaseService = mock(PurchaseService.class);
        financialService = mock(FinancialService.class);
        learningService = mock(LearningService.class);
        confirmPayment = new ConfirmPayment(
                paymentService,
                purchaseService,
                financialService,
                learningService
        );
        when(paymentService.confirmPayment(notification)).thenReturn(paymentTransaction);
        when(purchaseService.getPurchase(transactionId)).thenReturn(purchase);
    }

    @Test
    void confirm_the_payment() throws NotValidNotification, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData, LearningPlatformIsNotAvailable {
        confirmPayment.confirm(notification);

        verify(paymentService, times(1)).confirmPayment(any());
    }

    @Test
    void throw_not_valid_notification_when_payment_service_detect_is_not_valid() throws NotValidNotification {
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

    private Purchase createPurchaseFixture(int transactionId) {
        return new Purchase(transactionId, "RANDOM_ORDER_NAME");
    }

    private PaymentTransaction createPaymentTransactionFixture(int transactionId) {
        return new PaymentTransaction(
                transactionId,
                "RANDOM_IP",
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION, "RANDOM_TPV_TOKEN",
                1,
                "RANDOM_ORDER_NAME",
                34.56,
                "20231205103259",
                PaymentTransactionState.PENDING,
                new PaymentStatus());
    }

    private PaymentNotification createNotificationFixture() {
        return new PaymentNotification(
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION,
                1234567,
                "RANDOM_ORDER",
                "RANDOM_AMOUNT",
                "OK"
        );
    }
}
