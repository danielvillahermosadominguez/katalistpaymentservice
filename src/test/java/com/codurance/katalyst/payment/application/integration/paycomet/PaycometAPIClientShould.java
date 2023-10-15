package com.codurance.katalyst.payment.application.integration.paycomet;

import com.codurance.katalyst.payment.application.infrastructure.adapters.paycomet.PayCometApiClientAdapter;
import com.codurance.katalyst.payment.application.integration.wiremock.PayCometWireMockServer;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentOrder;
import com.codurance.katalyst.payment.application.model.ports.paycomet.exception.PayCometNotRespond;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PaycometAPIClientShould {
    public static final int WIREMOCK_PORT = 9003;
    public static final String RANDOM_API_KEY = "RANDOM_API_KEY";
    public static final int terminal = 1234;
    private String urlBase = "http://localhost:9003";
    private PayCometWireMockServer wireMock = null;
    private PayCometApiClientAdapter apiAdapter = new PayCometApiClientAdapter(new RestTemplate());

    @BeforeEach
    void beforeEach() {
        if (this.wireMock == null) {
            this.wireMock = new PayCometWireMockServer();
            this.apiAdapter.setURLBase(urlBase);
            this.apiAdapter.setAPIKey(RANDOM_API_KEY);
            this.apiAdapter.setTerminal(terminal);
            this.wireMock.setPort(WIREMOCK_PORT);
            this.wireMock.start();
        }
        this.wireMock.reset();
    }

    @AfterEach
    void afterEach() {
        this.wireMock.stop();
    }

    @Test
    void create_an_user_with_the_token() throws PayCometNotRespond {
        var idUser = 1;
        var tokenUser = "RANDOM_TOKEN_USER";
        var errorCode = 0;
        var jetToken = "RANDOM_TOKEN";
        var responseBody = String.format("""
                {
                    "idUser":%s,
                    "tokenUser": "%s",
                    "errorCode":%s
                }
                """, idUser, tokenUser, errorCode);

        wireMock.stubForCreateUserWithStatusOk(responseBody);

        var user = apiAdapter.createUser(jetToken);

        assertThat(user).isNotNull();
        assertThat(user.getIdUser()).isEqualTo(idUser);
        assertThat(user.getTokenUser()).isEqualTo(tokenUser);
        assertThat(user.getErrorCode()).isEqualTo(errorCode);
    }

    @Test
    void throw_an_exception_when_paycomet_not_respond_creating_an_user() throws JSONException {
        var jetToken = "RANDOM_TOKEN";

        var expectedRequestBody = String.format("""
                {
                    "terminal":[%s],
                    "jetToken": ["%s"]
                }
                """, terminal, jetToken);

        var exception = Assertions.assertThrows(PayCometNotRespond.class, () -> {
            apiAdapter.createUser(jetToken);
        });

        assertThat(exception).isNotNull();
        JSONAssert.assertEquals(expectedRequestBody, exception.getRequestBody(), true);
        assertThat(exception.getUrl()).isEqualTo(apiAdapter.generateEndPoint("/v1/cards"));
        assertThat(exception.getUrlVariables()).isEqualTo("");
    }

    @Test
    void authorize_a_payment() throws PayCometNotRespond {
        int errorCode = 0;
        int amount = 1032;
        var currency = "EUR";
        var order = "ORDER12345";
        var challengeUrl = "RANDOM_CHALLENGE_URL";
        var paymentOrder = new PaymentOrder(
                10.32,
                "EUR",
                1,
                1,
                "ORDER12345",
                "RANDOM_UP",
                "RANDOM_TOKEN_USER");

        var responseBody = String.format("""
                {
                    "errorCode":%s,
                    "amount":%s,
                    "currency": "%s",
                    "order": "%s",
                    "challengeUrl": "%s"
                }
                """, errorCode, amount, currency, order, challengeUrl);
        wireMock.stubForAuthorizePaymentWithStatusOk(responseBody);

        var paymentStatus = apiAdapter.authorizePayment(paymentOrder);

        assertThat(paymentStatus).isNotNull();
        assertThat(paymentStatus.getErrorCode()).isEqualTo(errorCode);
        assertThat(paymentStatus.getCurrency()).isEqualTo(currency);
        assertThat(paymentStatus.getChallengeUrl()).isEqualTo(challengeUrl);
        assertThat(paymentStatus.getOrder()).isEqualTo(order);
        assertThat(paymentStatus.getAmount()).isEqualTo(amount);
    }

    @Test
    void throw_an_exception_when_paycomet_not_respond_in_authorization() throws JSONException {
        var paymentOrder = new PaymentOrder(
                10.32,
                "EUR",
                1,
                1,
                "ORDER12345",
                "RANDOM_IP",
                "RANDOM_TOKEN_USER");

        var expectedRequestBody = String.format("""
                  {
                    "payment": {
                        "terminal": %s,
                        "amount": "%s",                        
                        "idUser": %s,                        
                        "originalIp": "%s",
                        "secure": 1,
                        "order": "%s",
                        "tokenUser": "%s",
                        "currency": "%s",
                        "merchantDescriptor": "%s",
                        "methodId": "%s",  
                        "productDescription":"%s"                  
                    }
                   }                                
                """, terminal, 1032, 1, "RANDOM_IP", "ORDER12345", "RANDOM_TOKEN_USER", "EUR", "Katalyst subscription", 1, "Katalyst subscription");

        var exception = Assertions.assertThrows(PayCometNotRespond.class, () -> {
            apiAdapter.authorizePayment(paymentOrder);
        });

        assertThat(exception).isNotNull();
        JSONAssert.assertEquals(expectedRequestBody, exception.getRequestBody(), true);
        assertThat(exception.getUrl()).isEqualTo(apiAdapter.generateEndPoint("/v1/payments"));
        assertThat(exception.getUrlVariables()).isEqualTo("");
    }
}
