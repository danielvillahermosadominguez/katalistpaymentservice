package com.codurance.katalyst.payment.application.ports;

import com.codurance.katalyst.payment.application.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentStatus;

public interface PayCometApiClient {
    CreatedUser createUser(String jetToken);
    PaymentStatus payment(double amount, String currency, int idUser, String methodId, String order, String originalIp, String tokenUser);
}
