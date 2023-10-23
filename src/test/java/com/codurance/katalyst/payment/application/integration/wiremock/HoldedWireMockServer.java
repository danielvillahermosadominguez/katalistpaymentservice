package com.codurance.katalyst.payment.application.integration.wiremock;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@Component
public class HoldedWireMockServer extends WireMockServerExtension {
    public String apiKey;
    private String URL_BASE = "/api/";

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void stubForCreateInvoiceWithStatusOK(String invoiceID,
                                                 Map<String, String> requestParameters,
                                                 String jsonResponseBody) throws UnsupportedEncodingException {
        stubForPostWithStatusOKAndBodyParameters("invoicing/v1/documents/invoice/" + unicode(invoiceID) + "/send",
                joinParameters(requestParameters),
                jsonResponseBody);
    }

    public void stubForCreateContactsWithStatusOKAsJsonBody(String jsonBodyParameters,
                                                            String jsonBody) {
        stubForPostWithStatusOKAndBodyJson("invoicing/v1/contacts",
                jsonBodyParameters,
                jsonBody);
    }

    public void stubForGetContactByCustomIdStatusOK(String customId,
                                                    String jsonResponse) throws UnsupportedEncodingException {
        var urlParameters = "?customId=" + URLEncoder.encode(customId, "UTF-8");
        this.wireMockServer.stubFor(
                get(urlEqualTo(String.format(URL_BASE + "invoicing/v1/contacts%s", urlParameters)))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withBody(jsonResponse)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                        )
        );
    }

    private void stubForPostWithStatusOKAndBodyParameters(String function,
                                                          String requestBody,
                                                          String responseBody) {
        this.wireMockServer.stubFor(
                post(urlEqualTo(URL_BASE + function))
                        .withRequestBody(containing(requestBody))
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


    public void stubForPostWithStatusOKAndBodyJson(String function,
                                                   String requestBody,
                                                   String responseBody) {
        this.wireMockServer.stubFor(
                post(urlEqualTo(URL_BASE + function))
                        .withRequestBody(equalToJson(requestBody))
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

    public void verifySendInvoiceHasBeenCalled(String invoiceID) throws UnsupportedEncodingException {
        this.wireMockServer.verify(1,
                postRequestedFor(
                        urlEqualTo(URL_BASE + "invoicing/v1/documents/invoice/" + unicode(invoiceID) + "/send")
                )
        );
    }
}
