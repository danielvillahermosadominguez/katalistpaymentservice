package com.codurance.katalyst.payment.application.unit.actions;

import com.codurance.katalyst.payment.application.actions.CancelPayment;
import com.codurance.katalyst.payment.application.builders.PaymentNotificationBuilder;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.exceptions.PaymentTransactionNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CancelPaymentShould {

    private PaymentService paymentService;
    private AbstractLog log;
    private CancelPayment cancelPayment;
    private PaymentNotificationBuilder builder;

    @BeforeEach
    void beforeEach() {
        builder = new PaymentNotificationBuilder();
        paymentService = mock(PaymentService.class);
        log = mock(AbstractLog.class);
        cancelPayment = new CancelPayment(paymentService, log);
    }

    @Test
    void cancel_the_payment_transaction_when_response_is_ko() throws PaymentTransactionNotFound {
        var notification = builder
                .createPaymentNotificationByDefault()
                .order("RANDOM_ORDER")
                .response("KO")
                .getItem();

        cancelPayment.cancel(notification);

        verify(paymentService, times(1))
                .cancelTransaction(eq("RANDOM_ORDER"));
    }

    @Test
    void not_cancel_the_transaction_when_response_is_not_ko() throws PaymentTransactionNotFound {
        var notification = builder
                .createPaymentNotificationByDefault()
                .order("RANDOM_ORDER")
                .response("OTHER_KIND_OF_RESPONSE_USUALLY_OK")
                .getItem();

        cancelPayment.cancel(notification);

        verify(paymentService, never()).cancelTransaction(any());
    }
}
