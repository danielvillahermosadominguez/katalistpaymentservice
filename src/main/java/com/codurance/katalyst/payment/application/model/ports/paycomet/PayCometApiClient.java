package com.codurance.katalyst.payment.application.model.ports.paycomet;

import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentOrder;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;

public interface PayCometApiClient {
    CreatedUser createUser(String jetToken);

    PaymentStatus payment(PaymentOrder paymentData);
}
