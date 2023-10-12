package com.codurance.katalyst.payment.application.unit.model;

import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;

public class PaymentServiceShould {

    private static final int TPV_ID = 1234;
    private static final int NOT_MY_TPV_ID = 44444;
    private PaymentService paymentService;
    private PaymentNotification notification;

    @BeforeEach
    void beforeEach() {
        paymentService = new PaymentService(TPV_ID);
        notification = new PaymentNotification(
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION,
                TPV_ID,
                "RANDOM_ORDER",
                "RANDOM_AMOUNT",
                "OK"
        );
    }

    @Test
    void throw_not_valid_notification_when_transaction_type_is_not_authorization() {
        notification.setTransactionType(TransactionType.CHARGEBACK);
        var exception = assertThrows(NotValidNotification.class, () -> {
            paymentService.confirmPayment(notification);
        });

        assertThat(exception).isNotNull();
    }

    @Test
    void throw_not_valid_notification_when_method_is_not_card() {
        notification.setMethod(PaymentMethod.BIZUM);
        var exception = assertThrows(NotValidNotification.class, () -> {
            paymentService.confirmPayment(notification);
        });

        assertThat(exception).isNotNull();
    }

    @Test
    void throw_not_valid_notification_when_tpv_is_not_correct() {
        notification.setTpvID(NOT_MY_TPV_ID);
        var exception = assertThrows(NotValidNotification.class, () -> {
            paymentService.confirmPayment(notification);
        });

        assertThat(exception).isNotNull();
    }
}
