package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.model.ports.paycomet.PayCometApiClient;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentData;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;

public class PayCometApiClientFake implements PayCometApiClient {

    public static String URL_CHALLENGE_OK ="URL_CHALLENGE_OK";

    public String generateTemporalToken() {
        return "RANDOM_TOKEN";
    }

    @Override
    public CreatedUser createUser(String jetToken) {
        var result = new CreatedUser();
        result.setIdUser(22);
        result.setTokenUser("RANDOM_TOKEN_USER");
        return result;
    }

    @Override
    public PaymentStatus payment(PaymentData paymentData) {
        var paymentStatus = new PaymentStatus();
        paymentStatus.setChallengeUrl(URL_CHALLENGE_OK);
        return paymentStatus;
    }
}
