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
        if (checkKOResponse(notification)) return;

        logWarningForCancelPayment(notification);
        try {
            paymentService.cancelTransaction(notification.getOrder());
        } catch (PaymentTransactionNotFound e) {
            logErrorForNotCancelPaymentOrderNotFound(notification);
        }
    }

    private boolean checkKOResponse(PaymentNotification notification) {
        var response = notification.getResponse();
        if (!response.equals(KO)) {
            logErrorForNotCancelPaymentNotificationNotKO(notification);
            return true;
        }
        return false;
    }

    private void logWarningForCancelPayment(PaymentNotification notification) {
        log(notification, "[CANCEL PAYMENT]: %s");
    }

    private void logErrorForNotCancelPaymentNotificationNotKO(PaymentNotification notification) {
        log(
                notification,
                String.format(
                        "[CANCEL PAYMENT]: The notification is not KO, the value is %s and the full notification",
                        notification.getResponse()
                ) + ": %s"
        );
    }

    private void logErrorForNotCancelPaymentOrderNotFound(PaymentNotification notification) {
        log(
                notification,
                String.format(
                        "[CANCEL PAYMENT]: The order %s has not been found y the database. The full notification",
                        notification.getOrder()
                ) + ": %s"
        );
    }

    private void log(PaymentNotification notification, String message) {
        var mapper = new ObjectMapper();
        String information = null;
        try {
            information = mapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            information = "Not possible to parse information";
        }
        log.error(CancelPayment.class, String.format(message, information));
    }
}
