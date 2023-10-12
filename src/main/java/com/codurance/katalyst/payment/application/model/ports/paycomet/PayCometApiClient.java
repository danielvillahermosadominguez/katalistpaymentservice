package com.codurance.katalyst.payment.application.model.ports.paycomet;

import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;

public interface PayCometApiClient {
    CreatedUser createUser(String jetToken);
    PaymentStatus payment(double amount, String currency, int idUser, String methodId, String order, String originalIp, String tokenUser);
}
