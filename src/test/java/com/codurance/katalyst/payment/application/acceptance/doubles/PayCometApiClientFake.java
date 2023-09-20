package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.ports.PayCometApiClient;

public class PayCometApiClientFake implements PayCometApiClient {
    public String generateTemporalToken() {
        return "RANDOM_TOKEN";
    }
}
