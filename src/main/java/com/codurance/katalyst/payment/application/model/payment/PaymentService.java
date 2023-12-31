package com.codurance.katalyst.payment.application.model.payment;

import com.codurance.katalyst.payment.application.actions.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.actions.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.payment.exceptions.PaymentTransactionNotFound;
import com.codurance.katalyst.payment.application.model.ports.clock.Clock;
import com.codurance.katalyst.payment.application.model.ports.paycomet.PayCometApiClient;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentOrder;
import com.codurance.katalyst.payment.application.model.ports.paycomet.exception.PayCometNotRespond;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PaymentService {

    public static final String DEFAULT_CURRENCY_EUR = "EUR";
    public static final String DATETIME_FORMAT_IN_STRING = "yyyyMMddHHmmssSSS";
    public static final String ORDER_PREFIX = "PAY";
    private final int terminal;
    private final TransactionRepository transactionRepository;
    private final PayCometApiClient payCometApiClient;
    private final Clock clock;
    private final AbstractLog log;

    public PaymentService(TransactionRepository transactionRepository,
                          PayCometApiClient payCometApiClient,
                          Clock clock,
                          AbstractLog log,
                          @Value("${paycomet.terminal}") int terminal
    ) {
        this.terminal = terminal;
        this.transactionRepository = transactionRepository;
        this.payCometApiClient = payCometApiClient;
        this.clock = clock;
        this.log = log;
    }

    public PaymentTransaction confirmPayment(PaymentNotification notification) throws NotValidNotification, PaymentTransactionNotFound {
        checkValidNotification(notification);
        var paymentTransaction = transactionRepository.getPendingPaymentTransactionBasedOn(notification.getOrder());
        if (paymentTransaction == null) {
            throw new PaymentTransactionNotFound();
        }
        paymentTransaction.setTransactionState(PaymentTransactionState.DONE);
        paymentTransaction = transactionRepository.save(paymentTransaction);
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

    public PaymentTransaction authorizeTransaction(String ip, String paytpvToken, double price) throws TPVTokenIsRequired, CreditCardNotValid, PayCometNotRespond {
        checkToken(paytpvToken);
        var tpvUser = getUser(paytpvToken);
        var date = generateNowInString();
        var order = generateOrderName(date);
        var paymentData = new PaymentOrder(
                price,
                DEFAULT_CURRENCY_EUR,
                tpvUser.getIdUser(),
                1, //TODO: Change for PaymentMethod.CARDS
                order,
                ip,
                tpvUser.getTokenUser()
        );

        var paymentStatus = payCometApiClient.authorizePayment(paymentData);
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

        return transactionRepository.save(paymentTransaction);
    }

    private String generateNowInString() {
        var formatter = DateTimeFormatter
                .ofPattern(DATETIME_FORMAT_IN_STRING)
                .withZone(ZoneId.systemDefault());
        return formatter.format(clock.getInstant());
    }

    private String generateOrderName(String date) {
        return ORDER_PREFIX + date;
    }

    private CreatedUser getUser(String paytpvToken) throws CreditCardNotValid, PayCometNotRespond {
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

    public List<PaymentTransaction> getRetryPayments() {
        return transactionRepository.getPaymentTransactionForRetry();
    }

    public void updateTransaction(PaymentTransaction paymentTransaction) {
        transactionRepository.save(paymentTransaction);
    }

    public void cancelTransaction(String order) throws PaymentTransactionNotFound {
        var paymentTransactions = transactionRepository.getTransactionsBasedOnOrder(order);
        if (paymentTransactions == null || paymentTransactions.isEmpty()) {
            throw new PaymentTransactionNotFound();
        }

        if (paymentTransactions.size() > 1) {
            log.warn(PaymentService.class, String.format("[CANCEL TRANSACTION] We have detected %s transaction with the order code %s. We will cancel all the pending or retry transactions", paymentTransactions.size(), order));
        }

        //We cancel all the transactions as safety. But we should have only one
        for (var transaction : paymentTransactions) {
            if (transaction.getTransactionState() == PaymentTransactionState.RETRY ||
                    transaction.getTransactionState() == PaymentTransactionState.PENDING) {
                transaction.setTransactionState(PaymentTransactionState.CANCEL);
                transactionRepository.save(transaction);
            }
        }
    }
}
