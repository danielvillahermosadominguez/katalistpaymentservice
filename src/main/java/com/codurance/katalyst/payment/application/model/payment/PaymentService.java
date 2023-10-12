package com.codurance.katalyst.payment.application.model.payment;

import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final int terminal;
    private final TransactionRepository transactionRepository;

    public PaymentService(TransactionRepository transactionRepository, @Value("${paycomet.terminal}") int terminal) {
        this.terminal = terminal;
        this.transactionRepository = transactionRepository;
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
}
