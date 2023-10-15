package com.codurance.katalyst.payment.application.integration.holded;

import com.codurance.katalyst.payment.application.acceptance.doubles.ClockStub;
import com.codurance.katalyst.payment.application.infrastructure.adapters.holded.HoldedApiClientAdapter;
import com.codurance.katalyst.payment.application.integration.wiremock.HoldedWireMockServer;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedInvoiceInfo;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedTypeContact;
import com.codurance.katalyst.payment.application.model.ports.holded.exceptions.HoldedNotRespond;
import com.codurance.katalyst.payment.application.model.ports.holded.exceptions.NotValidEMailFormat;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
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
    private String holdedApiKey = "RANDOM_API_KEY";
    private HoldedWireMockServer wireMock = null;
    private HoldedApiClientAdapter apiAdapter = new HoldedApiClientAdapter(new RestTemplate());

    @BeforeEach
    void beforeEach() {
        if (this.wireMock == null) {
            this.wireMock = new HoldedWireMockServer();
            this.apiAdapter.setApiKey(holdedApiKey);
            this.apiAdapter.setURLBase(urlBase);
            this.apiAdapter.setDateService(new ClockStub());
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

        wireMock.stubForGetContactByCustomIdStatusOK(customId, "[]");

        var contact = apiAdapter.getContactByCustomId(customId);

        assertThat(contact).isNull();
    }

    @Test
    public void get_a_contact_by_custom_id_when_the_contact_exists() throws UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat {
        var customId = "RANDOM_CUSTOM_ID";
        String nifDni = "46842041C";
        var responseBody = String.format("""
                [{
                    "id": 1,
                    "name":  "RANDOM_NAME",
                    "code":  "%s",                    
                    "customId":"%s",
                    "email": "RANDOM_USERNAME@email.com",
                    "vatnumber":null,
                    "type": "client"                    
                }]
                """, nifDni, customId);

        wireMock.stubForGetContactByCustomIdStatusOK(customId, responseBody);

        var contact = apiAdapter.getContactByCustomId(customId);

        assertThat(contact).isNotNull();
        assertThat(contact.getVatNumber()).isNull();
        assertThat(contact.getId()).isEqualTo("1");
        assertThat(contact.getEmail().getValue()).isEqualTo("RANDOM_USERNAME@email.com");
        assertThat(contact.getCode()).isEqualTo(nifDni);
        assertThat(contact.getCustomId()).isEqualTo(customId);
        assertThat(contact.getType()).isEqualTo(HoldedTypeContact.CLIENT);
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
        var customId = HoldedContact.buildCustomId(nifCif, email);
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

        var responseBodyGet = String.format("""
                [{
                    "id": 1,
                    "name":  "%s",
                    "code":  "%s",                   
                    "customId":"%s",
                    "email": "%s",
                    "vatnumber":null,
                    "type": "client"                    
                }]
                """, name, nifCif, customId, email.getValue());

        wireMock.stubForGetContactByCustomIdStatusOK(
                customId,
                responseBodyGet
        );
        wireMock.stubForCreateContactsWithStatusOKAsJsonBody(requestBodyParameters, responseBodyCreate);

        var contactResult = apiAdapter.createContact(contact);

        assertThat(contactResult).isNotNull();
        assertThat(contactResult.getId()).isEqualTo(contactId);
        assertThat(contactResult.getEmail()).isEqualTo(email);
        assertThat(contactResult.getCustomId()).isEqualTo(customId);
        assertThat(contactResult.getType()).isEqualTo(HoldedTypeContact.CLIENT);
        assertThat(contactResult.getVatNumber()).isNull();
        assertThat(contactResult.getCode()).isEqualTo(nifCif);
    }

    @Test
    public void create_company_contact_when_the_contact_not_exists() throws UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat {
        var contactId = "1";
        var email = new HoldedEmail("RANDOM_USER@email.com");
        var name = "RANDOM_NAME";
        var nifCif = "46842041C";
        var customId = HoldedContact.buildCustomId(
                nifCif,
                email
        );
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

        var responseBodyGet = String.format("""
                [{
                    "id": 1,
                    "name":  "%s",
                    "code":  null,                  
                    "customId": "%s",
                    "email": "%s",
                    "vatnumber": "%s",
                    "type": "client"                    
                }]
                """, name, customId, email.getValue(), nifCif);
        wireMock.stubForGetContactByCustomIdStatusOK(
                customId,
                responseBodyGet
        );
        wireMock.stubForCreateContactsWithStatusOKAsJsonBody(requestBodyParameters, responseBodyCreate);

        var contactResult = apiAdapter.createContact(contact);

        assertThat(contactResult).isNotNull();
        assertThat(contactResult.getId()).isEqualTo(contactId);
        assertThat(contactResult.getEmail()).isEqualTo(email);
        assertThat(contactResult.getCustomId()).isEqualTo(customId);
        assertThat(contactResult.getType()).isEqualTo(HoldedTypeContact.CLIENT);
        assertThat(contactResult.getVatNumber()).isEqualTo(nifCif);
        assertThat(contactResult.getCode()).isNull();
    }

    @Test
    public void throw_an_holded_exception_when_create_contact_not_respond() throws NotValidEMailFormat, JSONException {
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
        var expectedBody = """
                {
                    "name": "RANDOM_NAME",
                    "email": "RANDOM_USER@email.com",
                    "type": "client",
                    "vatnumber": "46842041C",
                    "code": "46842041C",
                    "CustomId": "fa0e4d59cd5b7f54c5152421865c26e1f42b9fe898f33c8c98e75e3863bd35ce",
                    "isperson": true
                }                                
                """.trim().strip();

        var thrown = Assertions.assertThrows(HoldedNotRespond.class, () -> {
            apiAdapter.createContact(contact);
        });

        assertThat(thrown).isNotNull();
        JSONAssert.assertEquals(thrown.getRequestBody(), expectedBody, true);
        assertThat(thrown.getUrl()).isEqualTo(apiAdapter.generateEndPoint("invoicing/v1/contacts"));
        assertThat(thrown.getUrlVariables()).isEqualTo("");
    }

    @Test
    public void create_an_invoice_based_on_a_contact() throws HoldedNotRespond {
        var contactId = "1";
        var jsonResponse = """
                {
                    "status": 1,
                  	"id":"1",
                  	"invoiceNum": "RANDOM_INVOICE_NUM"
                }
                """;
        var requestBodyParameters = """
                {
                    "docType": "invoice",
                    "contactId": "1",
                    "desc": "",
                    "date": 2323223,
                    "items": [{
                            "name": "TEST_COURSE",
                            "desc": "",
                            "units": 1,
                            "subtotal": 100.0
                        }
                    ]
                }
                """;

        wireMock.stubForPostWithStatusOKAndBodyJson("invoicing/v1/documents/invoice",
                requestBodyParameters,
                jsonResponse);


        var contact = new HoldedContact();
        contact.setId(contactId);

        var invoice = apiAdapter.createInvoice(
                contact,
                "TEST_COURSE",
                "",
                1,
                100);

        assertThat(invoice).isNotNull();
    }

    @Test
    public void throw_an_holded_exception_when_create_an_invoice_not_respond() throws JSONException {
        var contact = mock(HoldedContact.class);
        var expectedBody = """
                {
                    "docType": "invoice",
                    "contactId": "1",
                    "desc": "",
                    "date": 2323223,
                    "items": [{
                            "name": "TEST_COURSE",
                            "desc": "",
                            "units": 1,
                            "subtotal": 100.0
                        }
                    ]
                }
                """.trim().strip();
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

        JSONAssert.assertEquals(thrown.getRequestBody(), expectedBody, true);
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

        var invoice = mock(HoldedInvoiceInfo.class);
        when(invoice.getId()).thenReturn(invoiceID);

        var status = apiAdapter.sendInvoice(invoice, emails);
        assertThat(status).isNotNull();
        assertThat(status.getStatus()).isEqualTo(HoldedStatus.OK);
        wireMock.verifySendInvoiceHasBeenCalled(invoiceID);
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

        var invoice = mock(HoldedInvoiceInfo.class);
        when(invoice.getId()).thenReturn(invoiceID);

         var status = apiAdapter.sendInvoice(invoice, emails);
        assertThat(status).isNotNull();
        assertThat(status.getStatus()).isNotEqualTo(HoldedStatus.OK);

        wireMock.verifySendInvoiceHasBeenCalled(invoiceID);
    }

    @Test
    public void throw_an_holded_exception_when_send_an_invoice_not_respond() throws NotValidEMailFormat, JSONException {
        var emails = Arrays.asList(new HoldedEmail("RANDOM_EMAIL@EMAIL.COM"));
        var invoice = mock(HoldedInvoiceInfo.class);
        var expectedBody = String.format("""
                {
                    "emails":["RANDOM_EMAIL@EMAIL.COM"]
                }
                """);
        when(invoice.getId()).thenReturn("1");

        var thrown = Assertions.assertThrows(HoldedNotRespond.class, () -> {
            apiAdapter.sendInvoice(invoice, emails);
        });

        assertThat(thrown).isNotNull();
        JSONAssert.assertEquals(thrown.getRequestBody(),expectedBody, true);
        assertThat(thrown.getUrl()).isEqualTo(apiAdapter.generateEndPoint("invoicing/v1/documents/invoice/1/send"));
        assertThat(thrown.getUrlVariables()).isEqualTo("");
    }
}