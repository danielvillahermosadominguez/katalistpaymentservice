package com.codurance.katalyst.payment.application.unit.model;

import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.TransactionRepository;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceShould {

    private static final int TPV_ID = 1234;
    private static final int NOT_MY_TPV_ID = 44444;
    private PaymentService paymentService;
    private PaymentNotification notification;

    @Mock
    private TransactionRepository transactionRepository;

    @BeforeEach
    void beforeEach() {
        paymentService = new PaymentService(transactionRepository, TPV_ID);
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

    @Test
    void obtain_the_order_from_repository_when_order_exists() throws NotValidNotification {
        var orderName = "RANDOM_ORDER_NAME";
        int id = 1;
        var ip = "RANDOM_IP";
        var tpvUser = "RANDOM_TPV_USER";
        var amount = 34.56;
        var date = "20231205103259";
        var tpvToken = "RANDOM_TPV_TOKEN";
        var paymentTransactionExpected = new PaymentTransaction(
                id,
                ip,
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION,
                tpvToken,
                tpvUser,
                orderName,
                amount,
                date,
                PaymentTransactionState.PENDING
        );
        notification.setOrder(orderName);
        when(transactionRepository.getOpenTransactionBasedOn(orderName)).thenReturn(paymentTransactionExpected);

        var paymentTransaction = paymentService.confirmPayment(notification);

        verify(transactionRepository, times(1)).getOpenTransactionBasedOn(orderName);
        assertThat(paymentTransaction.getId()).isEqualTo(id);
        assertThat(paymentTransaction.getIp()).isEqualTo(ip);
        assertThat(paymentTransaction.getPaymentMethod()).isEqualTo(PaymentMethod.CARDS);
        assertThat(paymentTransaction.getTransactionType()).isEqualTo(TransactionType.AUTHORIZATION);
        assertThat(paymentTransaction.getTpvUser()).isEqualTo(tpvUser);
        assertThat(paymentTransaction.getTpvToken()).isEqualTo(tpvToken);
        assertThat(paymentTransaction.getOrder()).isEqualTo(orderName);
        assertThat(paymentTransaction.getAmount()).isEqualTo(amount);
        assertThat(paymentTransaction.getDate()).isEqualTo(date);
        assertThat(paymentTransaction.getState()).isEqualTo(PaymentTransactionState.PENDING);
    }

    @Test
    void obtain_the_order_from_repository_when_order_not_exists() throws NotValidNotification {
        var orderName = "RANDOM_ORDER_NAME";
        notification.setOrder(orderName);
        var paymentTransaction = paymentService.confirmPayment(notification);

        verify(transactionRepository, times(1)).getOpenTransactionBasedOn(orderName);
        assertThat(paymentTransaction).isNull();
    }
}
