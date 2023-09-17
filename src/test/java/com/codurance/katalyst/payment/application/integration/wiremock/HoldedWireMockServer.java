package com.codurance.katalyst.payment.application.integration.wiremock;

import com.codurance.katalyst.payment.application.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.utils.NotValidEMailFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
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


    public Map<String, Object> createContactResponseForGetContact(String email, String name, String nifCif) throws UnsupportedEncodingException, NotValidEMailFormat {
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        var mail = new HoldedEmail(email);
        var customId = URLEncoder.encode(nifCif + mail.getInUnicodeFormat(), "UTF-8");
        bodyMap.put("id", 1);
        bodyMap.put("customId", customId);
        bodyMap.put("email", email);
        bodyMap.put("name", name);
        bodyMap.put("code", nifCif);
        bodyMap.put("type", "");
        return bodyMap;
    }

    public Map<String, String> createContactRequestParameters(String name, String email, String type, String nifCif, String customId, String isPerson) {
        Map<String, String> requestBodyParameters = new LinkedHashMap();
        requestBodyParameters.put("name", name);
        requestBodyParameters.put("email", email);
        requestBodyParameters.put("type", type);
        requestBodyParameters.put("code", nifCif);
        requestBodyParameters.put("CustomId", customId);
        requestBodyParameters.put("isperson", isPerson);
        return requestBodyParameters;
    }

    public Map<String, Object> createResponseBodyOkCreate(String id) {
        Map<String, Object> responseBodyCreate = new LinkedHashMap<>();
        responseBodyCreate.put("status", 1);
        responseBodyCreate.put("info", "RANDOM_INFO");
        responseBodyCreate.put("id", id);
        return responseBodyCreate;
    }

    public Map<String, Object> createResponseBodyNotOK(String id) {
        Map<String, Object> responseBodyCreate = new LinkedHashMap<>();
        responseBodyCreate.put("status", 0);
        responseBodyCreate.put("info", "RANDOM_INFO");
        responseBodyCreate.put("id", id);
        return responseBodyCreate;
    }

    public Map<String, String> createRequestBodyForCreateInvoice(String contactId, String desc, String date) {
        Map<String, String> requestBodyParameters = new LinkedHashMap<>();
        requestBodyParameters.put("contactId", contactId + "");
        requestBodyParameters.put("desc", desc);
        requestBodyParameters.put("date", date);
        return requestBodyParameters;
    }

    public void stubForCreateInvoiceWithStatusOK(String invoiceID, Map<String, String> requestBodyMap, Map<String, Object> responseBody) throws UnsupportedEncodingException {
        var jsonResponseBody = gson.toJson(responseBody);
        stubForPostWithStatusOKAndBodyParameters("invoicing/v1/documents/invoice/" + unicode(invoiceID) + "/send",
                joinParameters(requestBodyMap),
                jsonResponseBody);
    }

    public void stubForCreateContactsWithStatusOK(Map<String, String> requestBodyParameters, Map<String, Object> responseBody) throws UnsupportedEncodingException {
        var jsonBody = gson.toJson(responseBody);
        stubForPostWithStatusOKAndBodyParameters("invoicing/v1/contacts",
                joinParameters(requestBodyParameters),
                jsonBody);
    }

    public void stubForCreateInvoiceWithStatusOk(Map<String, String> requestBodyParameters, Map<String, Object> responseBody) throws UnsupportedEncodingException {
        var jsonResponse = gson.toJson(responseBody);
        stubForPostWithStatusOKAndBodyParameters("invoicing/v1/documents/invoice",
                joinParameters(requestBodyParameters),
                jsonResponse);
    }

    public void stubForGetContactByCustomIdStatusOK(String customId, Map<String, Object> responseBody) throws UnsupportedEncodingException {
        var expectedListOfContact = Arrays.asList();
        if (responseBody != null) {
            expectedListOfContact = Arrays.asList(responseBody);
        }
        var jsonResponse = gson.toJson(expectedListOfContact.toArray());
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

    private void stubForPostWithStatusOKAndBodyParameters(String function, String requestBody, String responseBody) {
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

    public void verifySendInvoiceHasBeenCalled(String email, String invoiceID) throws UnsupportedEncodingException {
        this.wireMockServer.verify(1,
                postRequestedFor(urlEqualTo(URL_BASE + "invoicing/v1/documents/invoice/" + unicode(invoiceID) + "/send")));
    }
}
