package com.codurance.katalyst.payment.application.integration.wiremock;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class PayCometWireMockServer extends WireMockServerExtension {
    private final static String URL_BASE = "%s";

    public void stubForCreateUserWithStatusOk(String responseBody) {
        this.wireMockServer.stubFor(
                post(urlEqualTo(generateURL("/v1/cards")))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withBody(responseBody)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                        )
        );
    }

    public String generateURL(String function) {
        return String.format(URL_BASE, function);
    }

    public void stubForAuthorizePaymentWithStatusOk(String responseBody) {
        this.wireMockServer.stubFor(
                post(urlEqualTo(generateURL("/v1/payments")))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withBody(responseBody)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                        )
        );
    }
}
