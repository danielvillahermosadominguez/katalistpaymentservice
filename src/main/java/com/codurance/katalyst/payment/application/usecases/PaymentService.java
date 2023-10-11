package com.codurance.katalyst.payment.application.usecases;

import com.codurance.katalyst.payment.application.api.PaymentNotification;
import com.codurance.katalyst.payment.application.api.PaymentTransaction;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    public PaymentTransaction confirmPayment(PaymentNotification notification) {
        throw new UnsupportedOperationException();
    }
}
