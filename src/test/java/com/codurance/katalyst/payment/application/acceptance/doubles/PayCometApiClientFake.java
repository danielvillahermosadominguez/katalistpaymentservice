package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.ports.PayCometApiClient;

public class PayCometApiClientFake implements PayCometApiClient {
    public String generateTemporalToken() {
        return "RANDOM_TOKEN";
    }

    @Override
    public CreatedUser createUser(String jetToken) {
        CreatedUser result = new CreatedUser();
        result.setIdUser(22);
        result.setTokenUser("RANDOM_TOKEN_USER");
        return result;
    }

    @Override
    public PaymentStatus payment(double amount, String currency, int idUser, String methodId, String order, String originalIp, String tokenUser) {
        return new PaymentStatus();
    }
}
