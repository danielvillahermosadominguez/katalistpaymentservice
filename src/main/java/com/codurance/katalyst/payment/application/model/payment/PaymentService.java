package com.codurance.katalyst.payment.application.model.payment;

import com.codurance.katalyst.payment.application.actions.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.actions.exception.TPVTokenIsRequired;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class PaymentService {

    private final int terminal;
    private final TransactionRepository transactionRepository;
    private final PayCometApiClient payCometApiClient;
    private final Clock clock;

    public PaymentService(TransactionRepository transactionRepository,
                          PayCometApiClient payCometApiClient,
                          Clock clock, @Value("${paycomet.terminal}") int terminal
    ) {
        this.terminal = terminal;
        this.transactionRepository = transactionRepository;
        this.payCometApiClient = payCometApiClient;
        this.clock = clock;
    }

    public PaymentTransaction confirmPayment(PaymentNotification notification) throws NotValidNotification {
        checkValidNotification(notification);
        var paymentTransaction = transactionRepository.getOpenTransactionBasedOn(notification.getOrder());
        return paymentTransaction;
    }

    private void checkValidNotification(PaymentNotification notification) throws NotValidNotification {
        if (
                notification.getTransactionType() != TransactionType.AUTHORIZATION |
                        notification.getMethodId() != PaymentMethod.CARDS |
                        notification.getTpvID() != terminal
        ) {
            throw new NotValidNotification();
        }
    }

    public PaymentTransaction authorizeTransaction(String ip, String paytpvToken, double price) throws TPVTokenIsRequired, CreditCardNotValid {
        checkToken(paytpvToken);
        var tpvUser = getUser(paytpvToken);
        var date = generateNowInString();
        var order = generateOrderName(date);
        var paymentData = new PaymentData(
                price,
                "EUR",
                tpvUser.getIdUser(),
                1,
                order,
                ip,
                tpvUser.getTokenUser()
        );

        var paymentStatus = this.payCometApiClient.payment(paymentData);
        var paymentTransaction = new PaymentTransaction(
                ip,
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION,
                paytpvToken,
                tpvUser.getIdUser(),
                order,
                price,
                date,
                PaymentTransactionState.PENDING,
                paymentStatus);

        transactionRepository.save(paymentTransaction);
        return paymentTransaction;
    }

    private String generateNowInString() {
        var formatter = DateTimeFormatter
                .ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneId.systemDefault());
        return formatter.format(clock.getInstant());
    }

    private String generateOrderName(String date) {
        return "PAY" + date + "1";
    }

    private CreatedUser getUser(String paytpvToken) throws CreditCardNotValid {
        var tpvUser = payCometApiClient.createUser(paytpvToken);
        if (tpvUser == null) {
            throw new CreditCardNotValid();
        }
        return tpvUser;
    }

    private void checkToken(String paytpvToken) throws TPVTokenIsRequired {
        if (paytpvToken == null || paytpvToken.trim().isEmpty()) {
            throw new TPVTokenIsRequired();
        }
    }
}
