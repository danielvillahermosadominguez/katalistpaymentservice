package com.codurance.katalyst.payment.application.actions;

import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.exceptions.PaymentTransactionNotFound;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class CancelPayment {
    public static final String KO = "KO";
    private final PaymentService paymentService;
    private final AbstractLog log;

    public CancelPayment(PaymentService paymentService, AbstractLog log) {
        this.paymentService = paymentService;
        this.log = log;
    }

    public void cancel(PaymentNotification notification) {
        if (!isACorrectKOResponse(notification)) return;

        logWarningForCancelPayment(notification);
        try {
            paymentService.cancelTransaction(notification.getOrder());
        } catch (PaymentTransactionNotFound e) {
            logErrorForNotCancelPaymentOrderNotFound(notification);
        }
    }

    private boolean isACorrectKOResponse(PaymentNotification notification) {
        var response = notification.getResponse();
        if (!response.equals(KO)) {
            logErrorForNotCancelPaymentNotificationNotKO(notification);
            return false;
        }
        return true;
    }

    private void logWarningForCancelPayment(PaymentNotification notification) {
        log(notification, "[CANCEL PAYMENT]: This payment is going to be cancelled %s", false);
    }

    private void logErrorForNotCancelPaymentNotificationNotKO(PaymentNotification notification) {
        log(
                notification,
                String.format(
                        "[CANCEL PAYMENT]: The notification is not KO, the value is %s and the full notification",
                        notification.getResponse()
                ) + ": %s",
                true
        );
    }

    private void logErrorForNotCancelPaymentOrderNotFound(PaymentNotification notification) {
        log(
                notification,
                String.format(
                        "[CANCEL PAYMENT]: The order %s has not been found y the database. The full notification",
                        notification.getOrder()
                ) + ": %s",
                true
        );
    }

    private void log(PaymentNotification notification, String message, boolean error) {
        var mapper = new ObjectMapper();
        String information = null;
        try {
            information = mapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            information = "Not possible to parse information";
        }
        if(error) {
            log.error(CancelPayment.class, String.format(message, information));
        } else {
            log.warn(CancelPayment.class, String.format(message, information));
        }
    }
}
