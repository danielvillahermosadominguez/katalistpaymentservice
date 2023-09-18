package com.codurance.katalyst.payment.application.holded;

import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoiceItem;
import com.codurance.katalyst.payment.application.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.holded.exception.HoldedNotRespond;
import com.codurance.katalyst.payment.application.ports.HoldedApiClient;
import com.codurance.katalyst.payment.application.utils.APIClient;
import com.codurance.katalyst.payment.application.utils.DateService;
import com.codurance.katalyst.payment.application.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.holded.dto.NotValidEMailFormat;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class HoldedApiClientAdapter extends APIClient implements HoldedApiClient {

    public static final String CUSTOM_ID = "customId";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String TYPE = "type";
    public static final String CODE = "code";
    public static final String CUSTOM_ID1 = "CustomId";
    public static final String ISPERSON = "isperson";
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

    public HoldedApiClientAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setURLBase(String urlBase) {
        this.URL_BASE = urlBase;
    }

    public void setApiKey(String apiKey) {
        this.apyKey = apiKey;
    }

    public void setDateService(DateService dateService) {
        this.dateService = dateService;
    }

    @Override
    protected void getHeaderParameter(HttpHeaders headers) {
        headers.add(API_KEY_PARAM_NAME, apyKey);
    }
    public String generateEndPoint(String function) {
        return URL_BASE + function;
    }
    public HoldedContact getContactByCustomId(String customId) throws HoldedNotRespond {
        HoldedContact result = null;
        String url = generateEndPoint("invoicing/v1/contacts?customId={customId}");
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put(CUSTOM_ID, customId);

        var requestEntity = createRequest(null, MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<HoldedContact[]> response = null;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    HoldedContact[].class,
                    uriVariables);

            result = getFirst(response);
        }  catch (HttpStatusCodeException httpException) {
            throw new HoldedNotRespond(
                    url,
                    uriVariables.toString(),
                    "",
                    httpException.getMessage()
            );
        }

        return result;
    }

    public HoldedContact createContact(String name, String surname, HoldedEmail email, String company, String nifCif) throws UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat {
        HoldedContact result = null;
        var url = generateEndPoint("invoicing/v1/contacts");
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(NAME, name + " " + surname + "(" + company + ")");
        requestBody.add(EMAIL, email.getValue());
        requestBody.add(TYPE, CLIENT_VALUE);
        requestBody.add(CODE, nifCif);
        requestBody.add(CUSTOM_ID1, createCustomId(nifCif, email));
        requestBody.add(ISPERSON, "true");

        var request = createRequest(requestBody, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        ResponseEntity<HoldedStatus> response = null;
        try {
            response = restTemplate.postForEntity(url, request, HoldedStatus.class);
            if (response.getBody().getStatus() == HoldedStatus.OK) {
                var customId = createCustomId(nifCif, email);
                result = getContactByCustomId(customId);
            }
        }  catch (HttpStatusCodeException httpException) {
            throw new HoldedNotRespond(
                    url,
                    "",
                    requestBody.toString(),
                    httpException.getMessage()
            );
        }

        return result;
    }

    public HoldedCreationDataInvoice createInvoice(HoldedContact contact, String concept, String description, int amount, double price) throws HoldedNotRespond {
        HoldedCreationDataInvoice result = null;
        var url = generateEndPoint("invoicing/v1/documents/invoice");

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(CONTACT_ID, contact.getId());
        requestBody.add(DESC,description);
        var instant = dateService.getInstant();
        requestBody.add(DATE, instant.getEpochSecond()+"");
        var item = new HoldedCreationDataInvoiceItem(concept, "",amount, price);
        List<HoldedCreationDataInvoiceItem> items = Arrays.asList(item);
        var gson = new Gson();
        String jsonArray = gson.toJson(items);
        requestBody.add(ITEMS,jsonArray);

        var request = createRequest(requestBody, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        ResponseEntity<HoldedCreationDataInvoice> response = null;
        try {
            response = restTemplate.postForEntity(url, request, HoldedCreationDataInvoice.class);
            result = response.getBody();
        } catch (HttpStatusCodeException httpException) {
            throw new HoldedNotRespond(
                    url,
                    "",
                    requestBody.toString(),
                    httpException.getMessage()
            );
        }

        return result;
    }

    public HoldedStatus sendInvoice(HoldedCreationDataInvoice invoice, List<HoldedEmail> emails) throws HoldedNotRespond {
        String strEmails = HoldedEmail.getRecipients(emails);
        var url = generateEndPoint("invoicing/v1/documents/invoice/" + invoice.getId() + "/send");

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(EMAILS, strEmails);

        var request = createRequest(requestBody, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        ResponseEntity<HoldedStatus> response = null;
        try {
            response = restTemplate.postForEntity(url, request, HoldedStatus.class);
            return response.getBody();
        } catch (HttpStatusCodeException httpException) {
            throw new HoldedNotRespond(
                    url,
                    "",
                    requestBody.toString(),
                    httpException.getMessage()
            );
        }
    }

    public String createCustomId(String nifCif, HoldedEmail email) throws UnsupportedEncodingException, NotValidEMailFormat {
        return nifCif + email.getInUnicodeFormat();
    }
}
