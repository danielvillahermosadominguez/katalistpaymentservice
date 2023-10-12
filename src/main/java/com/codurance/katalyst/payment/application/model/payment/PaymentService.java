package com.codurance.katalyst.payment.application.model.payment;

import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    public PaymentTransaction confirmPayment(PaymentNotification notification) {
        throw new UnsupportedOperationException();
    }
}
