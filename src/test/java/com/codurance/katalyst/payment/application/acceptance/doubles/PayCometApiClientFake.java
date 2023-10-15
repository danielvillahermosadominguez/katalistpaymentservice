package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.model.ports.paycomet.PayCometApiClient;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentOrder;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;

import java.util.ArrayList;
import java.util.List;

public class PayCometApiClientFake implements PayCometApiClient {

    public static String URL_CHALLENGE_OK ="URL_CHALLENGE_OK";

    List<PaymentOrder> paymentOrders = new ArrayList<>();
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
    public PaymentStatus authorizePayment(PaymentOrder paymentOrder) {
        paymentOrders.add(paymentOrder);
        var paymentStatus = new PaymentStatus();
        paymentStatus.setOrder(paymentOrder.getOrder());
        paymentStatus.setCurrency("EUR");
        var toConvert = Double.valueOf((paymentOrder.getAmount() * 10));
        paymentStatus.setAmount(toConvert.intValue());
        paymentStatus.setErrorCode(0);
        paymentStatus.setChallengeUrl(URL_CHALLENGE_OK);
        return paymentStatus;
    }

    public List<PaymentOrder> getLastPaymentOrders() {
        return paymentOrders;
    }
    public void reset() {
        paymentOrders.clear();;
    }
}
