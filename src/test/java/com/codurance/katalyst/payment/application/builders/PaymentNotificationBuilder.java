package com.codurance.katalyst.payment.application.builders;

import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;

public class PaymentNotificationBuilder {
    private PaymentNotification item;


    public PaymentNotificationBuilder createPaymentNotificationByDefault() {
        item = createNotificationFixture();
        return this;
    }

    private PaymentNotification createNotificationFixture() {
        return new PaymentNotification(
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION,
                1234567,
                "RANDOM_ORDER",
                "RANDOM_AMOUNT",
                "OK"
        );
    }

    public PaymentNotification getItem() {
        return item;
    }

    public PaymentNotificationBuilder response(String value) {
        item.setResponse(value);
        return this;
    }

    public PaymentNotificationBuilder order(String value) {
        item.setOrder(value);
        return this;
    }
}
