package com.codurance.katalyst.payment.application.unit.actions;

import com.codurance.katalyst.payment.application.actions.ConfirmPayment;
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
        notification = new PaymentNotification(
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION,
                1234567,
                "RANDOM_ORDER",
                "RANDOM_AMOUNT",
                "OK"
        );
        int idTransaction = 12345;
        paymentTransaction = new PaymentTransaction(
                idTransaction,
                "RANDOM_IP",
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION, "RANDOM_TPV_TOKEN",
                "RANDOM_TPV_USER",
                "RANDOM_ORDER_NAME",
                34.56,
                "20231205103259",
                PaymentTransactionState.PENDING
        );
        paymentService = mock(PaymentService.class);
        purchaseService = mock(PurchaseService.class);
        financialService = mock(FinancialService.class);
        learningService = mock(LearningService.class);
        when(paymentService.confirmPayment(notification)).thenReturn(paymentTransaction);
        purchase = new Purchase(idTransaction);
        when(purchaseService.getPurchase(idTransaction)).thenReturn(purchase);
        confirmPayment = new ConfirmPayment(
                paymentService,
                purchaseService,
                financialService,
                learningService
        );
    }

    @Test
    void confirm_the_payment() throws NotValidNotification, NoCustomerData {
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
    void obtain_the_customer_related_to_the_payment_transaction() throws NotValidNotification, NoCustomerData {
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
    void emit_an_invoice_from_the_financial_service() throws NotValidNotification, NoCustomerData {
        when(financialService.emitInvoice(any())).thenReturn(true);

        confirmPayment.confirm(notification);

        verify(financialService, times(1)).emitInvoice(purchase);
        verify(purchaseService, times(1)).updateFinantialStepFor(purchase, true);
    }

    @Test
    void update_purchase_financial_step_not_passed() throws NotValidNotification, NoCustomerData {
        when(financialService.emitInvoice(any())).thenReturn(false);

        confirmPayment.confirm(notification);

        verify(financialService, times(1)).emitInvoice(purchase);
        verify(purchaseService, never()).updateFinantialStepFor(any(), anyBoolean());
    }

    @Test
    void acquire_a_course_with_the_purchase() throws NotValidNotification, NoCustomerData {
        when(learningService.acquireACourseFor(any())).thenReturn(true);

        confirmPayment.confirm(notification);

        verify(learningService, times(1)).acquireACourseFor(purchase);
        verify(purchaseService, times(1)).updateLearningStepFor(purchase, true);
    }

    @Test
    void update_purchase_learning_step_not_passed() throws NotValidNotification, NoCustomerData {
        when(learningService.acquireACourseFor(any())).thenReturn(false);

        confirmPayment.confirm(notification);

        verify(learningService, times(1)).acquireACourseFor(purchase);
        verify(purchaseService, never()).updateLearningStepFor(any(), anyBoolean());
    }
}