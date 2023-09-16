package com.codurance.katalyst.payment.application.integration;

import com.codurance.katalyst.payment.application.acceptance.utils.HoldedServiceFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestDateService;
import com.codurance.katalyst.payment.application.holded.HoldedAPIClient;
import com.codurance.katalyst.payment.application.holded.HoldedContactDTO;
import com.codurance.katalyst.payment.application.holded.HoldedInvoiceDTO;
import com.codurance.katalyst.payment.application.holded.HoldedInvoiceItemDTO;
import com.codurance.katalyst.payment.application.utils.DateService;
import com.codurance.katalyst.payment.application.utils.Mail;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.in;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HoldedAPIClientShould {
    public static final int WIREMOCK_HOLDED_PORT = 9001;
    Gson gson = new Gson();

    public static final String EQUAL_SYMBOL = "=";
    public static final String JOIN_SYMBOL = "&";

    private String holdedApiKey = "RANDOM_API_KEY";
    private HoldedServiceFake holdedService = null;

    private String urlBase = "http://localhost:9001/api/";

    HoldedAPIClient holdedAPIClient = new HoldedAPIClient(new RestTemplate());


    @BeforeEach
    void beforeEach() throws UnsupportedEncodingException {
        if (this.holdedService == null) {
            this.holdedService = new HoldedServiceFake();
            this.holdedAPIClient.setApiKey(holdedApiKey);
            this.holdedAPIClient.setURLBase(urlBase);
            this.holdedAPIClient.setDateService(new TestDateService());
            this.holdedService.setPort(WIREMOCK_HOLDED_PORT);
            this.holdedService.setToken(holdedApiKey);
            this.holdedService.start();
        }
        this.holdedService.resetAndConfigure();
    }

    @AfterEach
    void afterEach() {
        this.holdedService.stop();
    }

    @Test
    public void get_no_contact_by_custom_id_when_the_contact_not_exits() throws UnsupportedEncodingException {
        String json = "[]";
        String email = "RANDOM_USERNAME@email.com";
        Mail mail = new Mail(email);
        String nifCif = "46842041C";
        String customId = URLEncoder.encode(nifCif + mail.getInUnicodeFormat(), "UTF-8");
        String parameters = "?customId=" + customId;

        holdedService.stubForGetWithStatusOKAndBodyParameters(parameters, json);

        HoldedContactDTO contact = holdedAPIClient.getContactByCustomId(customId);

        assertThat(contact).isNull();
    }

    @Test
    public void get_contact_by_custom_id_when_the_contact_exits() throws UnsupportedEncodingException {
        String json = "";
        String email = "RANDOM_USERNAME@email.com";
        Mail mail = new Mail(email);
        String nifCif = "46842041C";
        String customId = nifCif + mail.getInUnicodeFormat();
        String parameters = "?customId=" + URLEncoder.encode(nifCif + mail.getInUnicodeFormat(), "UTF-8");

        Map<String, Object> expectedContact = createContact(email, "RANDOM_NAME", nifCif);
        json = gson.toJson(Arrays.asList(expectedContact).toArray());

        holdedService.stubForGetWithStatusOKAndBodyParameters(parameters, json);

        HoldedContactDTO contact = holdedAPIClient.getContactByCustomId(customId);

        assertThat(contact).isNotNull();
    }

    @Test
    public void create_contact_when_the_contact_exits() throws UnsupportedEncodingException {
        String email = "RANDOM_USER@email.com";
        Mail mail = new Mail(email);
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("status", 1);
        bodyMap.put("info", "RANDOM_INFO");
        bodyMap.put("id", "1");
        String jsonCreate = gson.toJson(bodyMap);

        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        String nifCif = "46842041C";
        String customId = nifCif + mail.getInUnicodeFormat();
        String name = "RANDOM_NAME";
        String surname = "RANDOM_SURNAME";
        String company = "RANDOM_COMPANY_NAME";


        requestBodyMap.put("name", name + " " + surname + "(" + company + ")");
        requestBodyMap.put("email", email);
        requestBodyMap.put("type", "client");
        requestBodyMap.put("code", nifCif);
        requestBodyMap.put("CustomId", customId);
        requestBodyMap.put("isperson", "true");

        Map<String, Object> expectedContact = createContact(email, "RANDOM_NAME", nifCif);
        String jsonGet = gson.toJson(Arrays.asList(expectedContact).toArray());
        String parameters = "?customId=" + URLEncoder.encode(nifCif + mail.getInUnicodeFormat(), "UTF-8");
        holdedService.stubForGetWithStatusOKAndBodyParameters(parameters, jsonGet);

        holdedService.stubForPostWithStatusOKAndBodyParameters("invoicing/v1/contacts",
                joinParameters(requestBodyMap),
                jsonCreate);

        HoldedContactDTO contact =  holdedAPIClient.createContact(name,surname, email,company,nifCif);
        assertThat(contact).isNotNull();
    }

    @Test
    public void create_an_invoice_based_on_a_contact() throws UnsupportedEncodingException {
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("id", 1);
        String json = gson.toJson(bodyMap);

        Map<String, String> requestBodyMap = new LinkedHashMap<>();

        requestBodyMap.put("contactId", "1");
        requestBodyMap.put("desc", "");
        requestBodyMap.put("date", "2323223");

        HoldedInvoiceItemDTO item = new HoldedInvoiceItemDTO("TEST_COURSE", 1, 100);

        List<HoldedInvoiceItemDTO> items = Arrays.asList(item);
        Gson gson = new Gson();
        String jsonArray = gson.toJson(items);
        requestBodyMap.put("items", jsonArray);

        holdedService.stubForPostWithStatusOKAndBodyParameters("invoicing/v1/documents/invoice",
                joinParameters(requestBodyMap),
                json);

        HoldedContactDTO holdedContact = mock(HoldedContactDTO.class);
        when(holdedContact.getId()).thenReturn("1");
        HoldedInvoiceDTO invoice = holdedAPIClient.createInvoice(holdedContact, "TEST_COURSE","",1,100);

        assertThat(invoice).isNotNull();
    }

    @Test
    public void send_an_invoice_to_an_email() throws UnsupportedEncodingException {
        String email = "RANDOM_USERNAME@gmail.com";
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("status", 1);
        bodyMap.put("info", "RANDOM_INFO");
        bodyMap.put("id", "1");
        String json = gson.toJson(bodyMap);

        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        requestBodyMap.put("emails", email);

        String invoiceID ="RANDOM_INVOIVCE_ID";
        holdedService.stubForPostWithStatusOKAndBodyParameters("invoicing/v1/documents/invoice/" + unicode(invoiceID) + "/send",
                joinParameters(requestBodyMap),
                json);

        HoldedInvoiceDTO invoice = mock(HoldedInvoiceDTO.class);
        when(invoice.getId()).thenReturn(invoiceID);
        holdedAPIClient.sendInvoice(invoice, email);

        holdedService.verifySendInvoiceHasBeenCalled(email, invoiceID);
    }

    protected String joinParameters(Map<String, String> requestBodyMap) throws UnsupportedEncodingException {
        String requestBody = "";

        for (Map.Entry<String, String> parameter : requestBodyMap.entrySet()) {
            requestBody += unicode(parameter.getKey()) + EQUAL_SYMBOL + unicode(parameter.getValue()) + JOIN_SYMBOL;
        }
        if (!requestBody.isEmpty()) {
            requestBody = requestBody.substring(0, requestBody.length() - 1);
        }

        return requestBody;
    }

    protected String unicode(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }

    private Map<String, Object> createContact(String email, String name, String nifCif) throws UnsupportedEncodingException {
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        Mail mail = new Mail(email);
        String customId = URLEncoder.encode(nifCif + mail.getInUnicodeFormat(), "UTF-8");
        bodyMap.put("id", 1);
        bodyMap.put("customId", customId);
        bodyMap.put("email", email);
        bodyMap.put("name", name);
        bodyMap.put("code", nifCif);
        bodyMap.put("type", "");
        return bodyMap;
    }
}