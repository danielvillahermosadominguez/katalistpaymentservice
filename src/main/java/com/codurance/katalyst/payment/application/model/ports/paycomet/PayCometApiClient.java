package com.codurance.katalyst.payment.application.model.ports.paycomet;

import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentOrder;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.model.ports.paycomet.exception.PayCometNotRespond;

public interface PayCometApiClient {
    CreatedUser createUser(String jetToken) throws PayCometNotRespond;

    PaymentStatus authorizePayment(PaymentOrder paymentData) throws PayCometNotRespond;
}
