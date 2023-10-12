package com.codurance.katalyst.payment.application.unit.model;

import com.codurance.katalyst.payment.application.actions.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.actions.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.TransactionRepository;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.ports.clock.Clock;
import com.codurance.katalyst.payment.application.model.ports.paycomet.PayCometApiClient;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentData;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceShould {

    private static final int TPV_ID = 1234;
    private static final int NOT_MY_TPV_ID = 44444;
    private PaymentService paymentService;
    private PaymentNotification notification;

    private PayCometApiClient payCometApiClient;


    private TransactionRepository transactionRepository;

    private Clock clock;
    @BeforeEach
    void beforeEach() {
        payCometApiClient = mock(PayCometApiClient.class);
        transactionRepository = mock(TransactionRepository.class);
        clock = mock(Clock.class);
        paymentService = new PaymentService(transactionRepository, payCometApiClient, clock, TPV_ID);
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
        var tpvUser = 123;
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
                PaymentTransactionState.PENDING,
                new PaymentStatus());
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
        assertThat(paymentTransaction.getPaymentStatus()).isNotNull();
    }

    @Test
    void obtain_the_order_from_repository_when_order_not_exists() throws NotValidNotification {
        var orderName = "RANDOM_ORDER_NAME";
        notification.setOrder(orderName);
        var paymentTransaction = paymentService.confirmPayment(notification);

        verify(transactionRepository, times(1)).getOpenTransactionBasedOn(orderName);
        assertThat(paymentTransaction).isNull();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void throw_an_exception_if_customer_data_dont_have_tpv_token(String token) {

        var thrown = Assertions.assertThrows(TPVTokenIsRequired.class, () -> {
            paymentService.authorizeTransaction("RANDOM_IP", token, 14.4);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_paycomet_dont_return_an_user() {
        var thrown = Assertions.assertThrows(CreditCardNotValid.class, () -> {
            paymentService.authorizeTransaction("RANDOM_IP", "RANDOM_TOKEN", 14.4);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void authorize_the_payment() throws TPVTokenIsRequired, CreditCardNotValid, ParseException {
        var paymentDataCatcher = ArgumentCaptor.forClass(PaymentData.class);
        var instant = createInstant("26-09-2023 18:00:00");
        var idUser = 3;
        var token = "RANDOM_TOKEN";
        var ip = "RANDOM_IP";
        var price = 14.4;
        var payCommetUser = new CreatedUser(idUser, token, 0);
        when(clock.getInstant()).thenReturn(instant);
        when(payCometApiClient.createUser(token)).thenReturn(payCommetUser);
        when(payCometApiClient.payment(any())
        ).thenReturn(new PaymentStatus());

        var paymentTransaction = paymentService.authorizeTransaction(ip, token, price);

        verify(payCometApiClient, times(1)).payment(
                paymentDataCatcher.capture()
        );

        assertThat(paymentTransaction).isNotNull();
        var paymentData = paymentDataCatcher.getValue();
        assertThat(paymentData.getAmount()).isEqualTo(price);
        assertThat(paymentData.getCurrency()).isEqualTo("EUR");
        assertThat(paymentData.getIdUser()).isEqualTo(idUser);
        assertThat(paymentData.getMethodId()).isEqualTo(1);
        assertThat(paymentData.getOriginalIp()).isEqualTo(ip);
        assertThat(paymentData.getTokenUser()).isEqualTo(token);
        assertThat(paymentData.getOrder()).isEqualTo("PAY202309261800001");
    }

    @Test
    void save_payment_transaction() throws TPVTokenIsRequired, CreditCardNotValid, ParseException {
        var paymentTransactionCatcher = ArgumentCaptor.forClass(PaymentTransaction.class);
        var instant = createInstant("26-09-2023 18:00:00");
        var idUser = 3;
        var token = "RANDOM_TOKEN";
        var ip = "RANDOM_IP";
        var price = 14.4;
        var payCommetUser = new CreatedUser(idUser, token, 0);
        when(clock.getInstant()).thenReturn(instant);
        when(payCometApiClient.createUser(token)).thenReturn(payCommetUser);
        when(payCometApiClient.payment(any())
        ).thenReturn(new PaymentStatus());

        var paymentTransaction = paymentService.authorizeTransaction(ip, token, price);

        verify(transactionRepository, times(1)).save(
                paymentTransactionCatcher.capture()
        );

        assertThat(paymentTransaction).isNotNull();
        var savedPaymentTransaction = paymentTransactionCatcher.getValue();
        assertThat(savedPaymentTransaction.getAmount()).isEqualTo(price);
        assertThat(savedPaymentTransaction.getIp()).isEqualTo(ip);
        assertThat(savedPaymentTransaction.getPaymentMethod()).isEqualTo(PaymentMethod.CARDS);
        assertThat(savedPaymentTransaction.getTpvUser()).isEqualTo(idUser);
        assertThat(savedPaymentTransaction.getTpvToken()).isEqualTo(token);
        assertThat(savedPaymentTransaction.getTransactionType()).isEqualTo(TransactionType.AUTHORIZATION);
        assertThat(savedPaymentTransaction.getOrder()).isEqualTo("PAY202309261800001");
        assertThat(savedPaymentTransaction.getDate()).isEqualTo("20230926180000");
        assertThat(savedPaymentTransaction.getState()).isEqualTo(PaymentTransactionState.PENDING);
        assertThat(paymentTransaction.getId()).isNotEqualTo(-1);
    }

    private Instant createInstant(String dateString) throws ParseException {
        var formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        var date = formatter.parse(dateString);
        var instant = date.toInstant();
        return instant;
    }
}
