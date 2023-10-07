package com.codurance.katalyst.payment.application.integration;

import com.codurance.katalyst.payment.application.acceptance.doubles.TestDateService;
import com.codurance.katalyst.payment.application.holded.HoldedApiClientAdapter;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoiceItem;
import com.codurance.katalyst.payment.application.integration.wiremock.HoldedWireMockServer;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedTypeContact;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.HoldedNotRespond;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.NotValidEMailFormat;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
    private HoldedApiClientAdapter apiAdapter = new HoldedApiClientAdapter(new RestTemplate());

    @BeforeEach
    void beforeEach() {
        if (this.wireMock == null) {
            this.wireMock = new HoldedWireMockServer();
            this.apiAdapter.setApiKey(holdedApiKey);
            this.apiAdapter.setURLBase(urlBase);
            this.apiAdapter.setDateService(new TestDateService());
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
    public void get_null_contact_by_custom_id_when_the_contact_not_exits() throws UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat {
        var email = new HoldedEmail("RANDOM_USERNAME@email.com");
        var nifCif = "46842041C";
        var customId = nifCif + email.getInUnicodeFormat();

        wireMock.stubForGetContactByCustomIdStatusOK(customId, null);

        var contact = apiAdapter.getContactByCustomId(customId);

        assertThat(contact).isNull();
    }

    @Test
    public void get_a_contact_by_custom_id_when_the_contact_exists() throws UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat {
        var email = new HoldedEmail("RANDOM_USERNAME@email.com");
        var nifCif = "46842041C";
        var customId = nifCif + email.getInUnicodeFormat();
        var responseBody = wireMock.createContactResponseForGetContact(
                "RANDOM_USERNAME@email.com",
                "RANDOM_NAME",
                nifCif,
                null,
                "client",
                false
        );

        wireMock.stubForGetContactByCustomIdStatusOK(customId, responseBody);

        var contact = apiAdapter.getContactByCustomId(customId);

        assertThat(contact).isNotNull();
    }

    @Test
    public void throw_an_holded_exception_when_get_a_contact_by_custom_id_not_respond() {
        var thrown = Assertions.assertThrows(HoldedNotRespond.class, () -> {
            apiAdapter.getContactByCustomId("RANDOM_CUSTOM_ID");
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getRequestBody()).isEqualTo("");
        assertThat(thrown.getUrl()).isEqualTo(
                apiAdapter.generateEndPoint("invoicing/v1/contacts?customId={customId}")
        );
        assertThat(thrown.getUrlVariables()).isEqualTo("{customId=RANDOM_CUSTOM_ID}");
    }

    @Test
    public void create_person_contact_when_the_contact_not_exists() throws UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat {
        var contactId = "1";
        var email = new HoldedEmail("RANDOM_USER@email.com");
        var nifCif = "46842041C";
        var name = "RANDOM_NAME";
        var contact = new HoldedContact(
                name,
                nifCif,
                null,
                HoldedTypeContact.CLIENT,
                true,
                email,
                null,
                null,
                ""
        );

        var responseBodyCreate = wireMock.createResponseBodyOkCreate(contactId);

        var requestBodyParameters = wireMock.createContactRequestParameters(
                contact.getName(),
                contact.getEmail().getValue(),
                "client",
                contact.getCode(),
                contact.getVatNumber(),
                contact.getCustomId(),
                true
        );

        var responseBodyGet = wireMock.createContactResponseForGetContact(
                email.getValue(),
                name,
                nifCif,
                null,
                "client",
                false
        );
        wireMock.stubForGetContactByCustomIdStatusOK(
                HoldedContact.buildCustomId(
                        nifCif,
                        email
                ),
                responseBodyGet
        );
        wireMock.stubForCreateContactsWithStatusOKAsJsonBody(requestBodyParameters, responseBodyCreate);

        var contactResult = apiAdapter.createContact(contact);

        assertThat(contactResult).isNotNull();
        assertThat(contactResult.getId()).isEqualTo(contactId);
    }

    @Test
    public void create_company_contact_when_the_contact_not_exists() throws UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat {
        var contactId = "1";
        var email = new HoldedEmail("RANDOM_USER@email.com");
        var name = "RANDOM_NAME";
        var nifCif = "46842041C";
        var contact = new HoldedContact(
                name,
                null,
                nifCif,
                HoldedTypeContact.CLIENT,
                false,
                email,
                null,
                null,
                ""
        );

        var responseBodyCreate = wireMock.createResponseBodyOkCreate(contactId);

        var requestBodyParameters = wireMock.createContactRequestParameters(
                contact.getName(),
                contact.getEmail().getValue(),
                "client",
                contact.getCode(),
                contact.getVatNumber(),
                contact.getCustomId(),
                false
        );

        var responseBodyGet = wireMock.createContactResponseForGetContact(
                email.getValue(),
                name,
                null,
                nifCif,
                "client",
                false);
        wireMock.stubForGetContactByCustomIdStatusOK(
                HoldedContact.buildCustomId(
                        nifCif,
                        email
                ),
                responseBodyGet
        );
        wireMock.stubForCreateContactsWithStatusOKAsJsonBody(requestBodyParameters, responseBodyCreate);

        var contactResult = apiAdapter.createContact(contact);

        assertThat(contactResult).isNotNull();
        assertThat(contactResult.getId()).isEqualTo(contactId);
    }

    @Test
    public void throw_an_holded_exception_when_create_contact_not_respond() throws NotValidEMailFormat {
        var contact = new HoldedContact(
                "RANDOM_NAME",
                "46842041C",
                "46842041C", HoldedTypeContact.CLIENT,
                true,
                new HoldedEmail("RANDOM_USER@email.com"),
                null,
                null,
                ""
        );
        var thrown = Assertions.assertThrows(HoldedNotRespond.class, () -> {
            apiAdapter.createContact(contact);
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getRequestBody()).isEqualTo(
                "{\"name\":\"RANDOM_NAME\",\"email\":\"RANDOM_USER@email.com\",\"type\":\"client\",\"vatnumber\":\"46842041C\",\"code\":\"46842041C\",\"CustomId\":\"fa0e4d59cd5b7f54c5152421865c26e1f42b9fe898f33c8c98e75e3863bd35ce\",\"isperson\":true}"
        );
        assertThat(thrown.getUrl()).isEqualTo(apiAdapter.generateEndPoint("invoicing/v1/contacts"));
        assertThat(thrown.getUrlVariables()).isEqualTo("");
    }

    @Test
    public void create_an_invoice_based_on_a_contact() throws UnsupportedEncodingException, HoldedNotRespond {
        Integer contactId = 1;
        Integer invoiceId = 1;
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("id", invoiceId);

        Map<String, String> requestBodyParameters = wireMock.createRequestBodyForCreateInvoice(
                contactId + "",
                "",
                "2323223"
        );

        var item = new HoldedCreationDataInvoiceItem("TEST_COURSE","", 1, 100);
        var items = Arrays.asList(item);
        var jsonArray = gson.toJson(items);
        requestBodyParameters.put("items", jsonArray);

        wireMock.stubForCreateInvoiceWithStatusOk(requestBodyParameters, responseBody);

        var contact = mock(HoldedContact.class);
        when(contact.getId()).thenReturn(contactId + "");

        var invoice = apiAdapter.createInvoice(
                contact,
                "TEST_COURSE",
                "",
                1,
                100);

        assertThat(invoice).isNotNull();
    }

    @Test
    public void throw_an_holded_exception_when_create_an_invoice_not_respond() {
        var contact = mock(HoldedContact.class);
        when(contact.getId()).thenReturn("1");

        var thrown = Assertions.assertThrows(HoldedNotRespond.class, () -> {
            apiAdapter.createInvoice(
                    contact,
                    "TEST_COURSE",
                    "",
                    1,
                    100);
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getRequestBody()).isEqualTo(
                "{contactId=[1], desc=[], date=[2323223], items=[[{\"name\":\"TEST_COURSE\",\"desc\":\"\",\"units\":1,\"subtotal\":100.0}]]}"
        );
        assertThat(thrown.getUrl()).isEqualTo(apiAdapter.generateEndPoint("invoicing/v1/documents/invoice"));
        assertThat(thrown.getUrlVariables()).isEqualTo("");
    }

    @Test
    public void send_an_invoice_to_an_email_with_ok_status() throws UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat {
        var emails = Arrays.asList(new HoldedEmail("RANDOM_USERNAME@gmail.com"));
        var recipient = HoldedEmail.getRecipients(emails);
        var invoiceID = "1";
        Map<String, Object> responseBody = wireMock.createResponseBodyOkCreate(invoiceID);

        Map<String, String> requestBodyParameters = new LinkedHashMap<>();
        requestBodyParameters.put("emails",recipient);

        wireMock.stubForCreateInvoiceWithStatusOK(invoiceID, requestBodyParameters, responseBody);

        var invoice = mock(HoldedCreationDataInvoice.class);
        when(invoice.getId()).thenReturn(invoiceID);

        var status = apiAdapter.sendInvoice(invoice, emails);
        assertThat(status).isNotNull();
        assertThat(status.getStatus()).isEqualTo(HoldedStatus.OK);
        wireMock.verifySendInvoiceHasBeenCalled(recipient, invoiceID);
    }

    @Test
    public void send_an_invoice_to_an_email_with_not_ok_status() throws UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat {
        var emails = Arrays.asList(new HoldedEmail("RANDOM_USERNAME@gmail.com"));
        var recipient = HoldedEmail.getRecipients(emails);
        var invoiceID = "1";
        Map<String, Object> responseBody = wireMock.createResponseBodyNotOK(invoiceID);

        Map<String, String> requestBodyParameters = new LinkedHashMap<>();
        requestBodyParameters.put("emails", recipient);

        wireMock.stubForCreateInvoiceWithStatusOK(invoiceID, requestBodyParameters, responseBody);

        var invoice = mock(HoldedCreationDataInvoice.class);
        when(invoice.getId()).thenReturn(invoiceID);

         var status = apiAdapter.sendInvoice(invoice, emails);
        assertThat(status).isNotNull();
        assertThat(status.getStatus()).isNotEqualTo(HoldedStatus.OK);

        wireMock.verifySendInvoiceHasBeenCalled(recipient, invoiceID);
    }

    @Test
    public void throw_an_holded_exception_when_send_an_invoice_not_respond() throws NotValidEMailFormat {
        var emails = Arrays.asList(new HoldedEmail("RANDOM_EMAIL@EMAIL.COM"));
        var invoice = mock(HoldedCreationDataInvoice.class);
        when(invoice.getId()).thenReturn("1");

        var thrown = Assertions.assertThrows(HoldedNotRespond.class, () -> {
            apiAdapter.sendInvoice(invoice, emails);
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getRequestBody()).isEqualTo(
                "{emails=[RANDOM_EMAIL@EMAIL.COM]}"
        );
        assertThat(thrown.getUrl()).isEqualTo(apiAdapter.generateEndPoint("invoicing/v1/documents/invoice/1/send"));
        assertThat(thrown.getUrlVariables()).isEqualTo("");
    }
}