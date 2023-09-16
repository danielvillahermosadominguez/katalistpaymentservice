package com.codurance.katalyst.payment.application.integration;

import com.codurance.katalyst.payment.application.acceptance.utils.TestDateService;
import com.codurance.katalyst.payment.application.holded.HoldedApiClientImpl;
import com.codurance.katalyst.payment.application.holded.HoldedContactDTO;
import com.codurance.katalyst.payment.application.holded.HoldedInvoiceDTO;
import com.codurance.katalyst.payment.application.holded.HoldedInvoiceItem;
import com.codurance.katalyst.payment.application.integration.wiremock.HoldedWireMockServer;
import com.codurance.katalyst.payment.application.utils.EMail;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HoldedAPIClientShould {
    public static final int WIREMOCK_PORT = 9001;
    private String urlBase = "http://localhost:9001/api/";
    private Gson gson = new Gson();
    private String holdedApiKey = "RANDOM_API_KEY";
    private HoldedWireMockServer wireMock = null;
    private HoldedApiClientImpl apiClient = new HoldedApiClientImpl(new RestTemplate());


    @BeforeEach
    void beforeEach() {
        if (this.wireMock == null) {
            this.wireMock = new HoldedWireMockServer();
            this.apiClient.setApiKey(holdedApiKey);
            this.apiClient.setURLBase(urlBase);
            this.apiClient.setDateService(new TestDateService());
            this.wireMock.setPort(WIREMOCK_PORT);
            this.wireMock.setApiKey(holdedApiKey);
            this.wireMock.start();
        }
        this.wireMock.reset();
    }

    @AfterEach
    void afterEach() {
        this.wireMock.stop();
    }

    @Test
    public void get_null_contact_by_custom_id_when_the_contact_not_exits() throws UnsupportedEncodingException {
        var email = new EMail("RANDOM_USERNAME@email.com");
        var nifCif = "46842041C";
        var customId = nifCif + email.getInUnicodeFormat();

        wireMock.stubForGetContactByCustomIdStatusOK(customId, null);

        var contact = apiClient.getContactByCustomId(customId);

        assertThat(contact).isNull();
    }

    @Test
    public void get_a_contact_by_custom_id_when_the_contact_exists() throws UnsupportedEncodingException {
        var email = new EMail("RANDOM_USERNAME@email.com");
        var nifCif = "46842041C";
        var customId = nifCif + email.getInUnicodeFormat();
        var responseBody = wireMock.createContactResponseForGetContact(
                "RANDOM_USERNAME@email.com",
                "RANDOM_NAME",
                nifCif);
        wireMock.stubForGetContactByCustomIdStatusOK(customId, responseBody);

        var contact = apiClient.getContactByCustomId(customId);

        assertThat(contact).isNotNull();
    }

    @Test
    public void create_contact_when_the_contact_exits() throws UnsupportedEncodingException {
        var email = new EMail("RANDOM_USER@email.com");
        var nifCif = "46842041C";
        var customId = nifCif + email.getInUnicodeFormat();
        var name = "RANDOM_NAME";
        var surname = "RANDOM_SURNAME";
        var company = "RANDOM_COMPANY_NAME";
        var contactId = "1";
        var responseBodyCreate = wireMock.createResponseBodyOkCreate(contactId);

        var requestBodyParameters = wireMock.createContactRequestParameters(
                name + " " + surname + "(" + company + ")",
                email.getValue(),
                "client",
                nifCif,
                customId,
                "true"
        );

        var responseBodyGet = wireMock.createContactResponseForGetContact(
                "RANDOM_USER@email.com",
                "RANDOM_NAME",
                nifCif);
        wireMock.stubForGetContactByCustomIdStatusOK(customId, responseBodyGet);
        wireMock.stubForCreateContactsWithStatusOK(requestBodyParameters, responseBodyCreate);

        var contact = apiClient.createContact(
                name,
                surname,
                email.getValue(),
                company,
                nifCif
        );

        assertThat(contact).isNotNull();
    }

    @Test
    public void create_an_invoice_based_on_a_contact() throws UnsupportedEncodingException {
        Integer contactId = 1;
        Integer invoiceId = 1;
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("id", invoiceId);

        Map<String, String> requestBodyParameters = wireMock.createRequestBodyForCreateInvoice(
                contactId + "",
                "",
                "2323223"
        );

        var item = new HoldedInvoiceItem("TEST_COURSE", 1, 100);
        var items = Arrays.asList(item);
        var jsonArray = gson.toJson(items);
        requestBodyParameters.put("items", jsonArray);

        wireMock.stubForCreateInvoiceWithStatusOk(requestBodyParameters, responseBody);

        var contact = mock(HoldedContactDTO.class);
        when(contact.getId()).thenReturn(contactId + "");

        var invoice = apiClient.createInvoice(
                contact,
                "TEST_COURSE",
                "",
                1,
                100);

        assertThat(invoice).isNotNull();
    }

    @Test
    public void send_an_invoice_to_an_email() throws UnsupportedEncodingException {
        var emails = "RANDOM_USERNAME@gmail.com";
        var invoiceID = "1";
        Map<String, Object> responseBody = wireMock.createResponseBodyOkCreate(invoiceID);

        Map<String, String> requestBodyParameters = new LinkedHashMap<>();
        requestBodyParameters.put("emails", emails);

        wireMock.stubForCreateInvoiceWithStatusOK(invoiceID, requestBodyParameters, responseBody);

        var invoice = mock(HoldedInvoiceDTO.class);
        when(invoice.getId()).thenReturn(invoiceID);

        apiClient.sendInvoice(invoice, emails);

        wireMock.verifySendInvoiceHasBeenCalled(emails, invoiceID);
    }
}