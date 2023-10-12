package com.codurance.katalyst.payment.application.model.payment;

import com.codurance.katalyst.payment.application.actions.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.actions.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.ports.paycomet.PayCometApiClient;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.CreatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final int terminal;
    private final TransactionRepository transactionRepository;
    private final PayCometApiClient payCometApiClient;

    public PaymentService(TransactionRepository transactionRepository,
                          PayCometApiClient payCometApiClient,
                          @Value("${paycomet.terminal}") int terminal
    ) {
        this.terminal = terminal;
        this.transactionRepository = transactionRepository;
        this.payCometApiClient = payCometApiClient;
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

    public PaymentTransaction authorizeTransaction(String ip, String paytpvToken) throws TPVTokenIsRequired, CreditCardNotValid {
        checkToken(paytpvToken);
        var tpvUser = getUser(paytpvToken);
        return null;
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
