package com.codurance.katalyst.payment.application.holded;

import com.codurance.katalyst.payment.application.utils.APIClient;
import com.codurance.katalyst.payment.application.utils.DateService;
import com.codurance.katalyst.payment.application.utils.DateServiceLocalUTC;
import com.codurance.katalyst.payment.application.utils.Mail;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


@Component
public class HoldedAPIClient extends APIClient {

    public static final String CUSTOM_ID = "customId";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String TYPE = "type";
    public static final String CODE = "code";
    public static final String CUSTOM_ID1 = "CustomId";
    public static final String ISPERSON = "isperson";
    public static final int OK = 1;
    public static final String CLIENT_VALUE = "client";
    public static final String CONTACT_ID = "contactId";
    public static final String DESC = "desc";
    public static final String DATE = "date";
    public static final String ITEMS = "items";
    public static final String EMAILS = "emails";
    public static final String API_KEY_PARAM_NAME = "key";
    @Value("${holded.urlbase}")
    private String URL_BASE;

    @Value("${holded.apikey}")
    private String apyKey;

    @Autowired
    DateService dateService;

    @Override
    protected void getHeaderParameter(HttpHeaders headers) {
        headers.add(API_KEY_PARAM_NAME, apyKey);
    }
    private String generateEndPoint(String function) {
        return URL_BASE + function;
    }
    public HoldedContactDTO getContactByCustomId(String customId) {
        HoldedContactDTO result = null;
        String url = generateEndPoint("invoicing/v1/contacts?customId={customId}");
        Map<String, String> vars = new HashMap<>();
        vars.put(CUSTOM_ID, customId);

        HttpEntity<MultiValueMap<String, String>> request = createRequest(null, MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<HoldedContactDTO[]> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, request, HoldedContactDTO[].class, vars);

            result = getFirst(response);
        } catch (Exception ex) {
            // Use log and throw exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public HoldedContactDTO createContact(String name, String surname, String email, String company, String dnicif) throws UnsupportedEncodingException {
        HoldedContactDTO result = null;
        String url = generateEndPoint("invoicing/v1/contacts");
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(NAME, name + " " + surname + "(" + company + ")");
        map.add(EMAIL, email);
        map.add(TYPE, CLIENT_VALUE);
        map.add(CODE, dnicif);
        map.add(CUSTOM_ID1, createCustomId(dnicif, email));
        map.add(ISPERSON, "true");

        HttpEntity<MultiValueMap<String, String>> request = createRequest(map, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        ResponseEntity<HoldedResponse> response = null;
        try {
            response = restTemplate.postForEntity(url, request, HoldedResponse.class);
            if (response.getBody().getStatus() == OK) {
                String customId = createCustomId(dnicif, email);
                result = getContactByCustomId(customId);
            }
        } catch (Exception ex) {
            // Use log and throw exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public HoldedInvoiceDTO createInvoice(HoldedContactDTO contact, String concept, String description, int amount, double price) {
        HoldedInvoiceDTO result = null;
        String url = generateEndPoint("invoicing/v1/documents/invoice");

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add(CONTACT_ID, contact.getId());
        map.add(DESC,description);
        Instant instant = dateService.getInstant();
        map.add(DATE, instant.getEpochSecond()+"");
        HoldedInvoiceItemDTO item = new HoldedInvoiceItemDTO(concept, amount, price);
        List<HoldedInvoiceItemDTO> items = Arrays.asList(item);
        Gson gson = new Gson();
        String jsonArray = gson.toJson(items);
        map.add(ITEMS,jsonArray);

        HttpEntity<MultiValueMap<String, Object>> request = createRequest(map,MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        ResponseEntity<HoldedInvoiceDTO> response = null;
        try {
            response = restTemplate.postForEntity(url, request, HoldedInvoiceDTO.class);
            result = response.getBody();
        } catch (Exception ex) {
            // Use log and throw exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public void sendInvoice(HoldedInvoiceDTO invoice, String emails) {
        String url = generateEndPoint("invoicing/v1/documents/invoice/"+invoice.getId()+"/send");

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add(EMAILS, emails);

        HttpEntity<MultiValueMap<String, Object>> request = createRequest(map,MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        ResponseEntity<HoldedResponse> response = null;
        try {
            response = restTemplate.postForEntity(url, request, HoldedResponse.class);
            if (response.getBody().getStatus() != OK) {
                // Use log and throw exception
                String errorMessage = "";
            }
        } catch (Exception ex) {
            // Use log and throw exception
            String errorMessage = ex.getMessage();
        }

    }

    public String createCustomId(String nifCif, String email) throws UnsupportedEncodingException {
        Mail mail = new Mail(email);
        return nifCif + mail.getInUnicodeFormat();
    }
}
