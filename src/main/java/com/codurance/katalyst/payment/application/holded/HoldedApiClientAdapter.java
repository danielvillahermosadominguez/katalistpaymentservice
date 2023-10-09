package com.codurance.katalyst.payment.application.holded;

import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoiceItem;
import com.codurance.katalyst.payment.application.holded.requests.CreateContactRequestBody;
import com.codurance.katalyst.payment.application.holded.requests.CreateInvoiceRequestBody;
import com.codurance.katalyst.payment.application.ports.Holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.HoldedNotRespond;
import com.codurance.katalyst.payment.application.utils.APIClient;
import com.codurance.katalyst.payment.application.utils.DateService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class HoldedApiClientAdapter extends APIClient implements HoldedApiClient {

    public static final String CUSTOM_ID = "customId";
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
        var url = generateEndPoint("invoicing/v1/contacts?customId={customId}");
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put(CUSTOM_ID, customId);

        var requestEntity = createRequest(
                null,
                MediaType.APPLICATION_JSON_VALUE
        );

        HoldedContact result = null;
        try {
            var response = restTemplate.exchange(
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

    public HoldedContact createContact(HoldedContact contact) throws HoldedNotRespond {
        var url = generateEndPoint("invoicing/v1/contacts");
        var requestBody = new CreateContactRequestBody(contact);
        var request = createRequestEntity(
                requestBody,
                MediaType.APPLICATION_JSON_VALUE
        );

        HoldedContact result = null;
        try {
            var response = restTemplate.postForEntity(
                    url,
                    request,
                    HoldedStatus.class
            );

            var body = response.getBody();
            if (body.getStatus() == HoldedStatus.OK) {
                result = getContactByCustomId(contact.getCustomId());
            }
        }  catch (HttpStatusCodeException httpException) {
            throw new HoldedNotRespond(
                    url,
                    "",
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }

        return result;
    }

    public HoldedCreationDataInvoice createInvoice(HoldedContact contact, String concept, String description, int amount, double price) throws HoldedNotRespond {
        var url = generateEndPoint("invoicing/v1/documents/invoice");
        var instant = dateService.getInstant();
        var invoiceItem = new HoldedCreationDataInvoiceItem(
                concept,
                "",
                amount,
                price
        );
        var items = Arrays.asList(invoiceItem);
        var requestBody = new CreateInvoiceRequestBody(
                contact.getId(),
                description,
                instant.getEpochSecond(),
                items
        );
        var request = createRequestEntity(
                requestBody,
                MediaType.APPLICATION_JSON_VALUE
        );

        HoldedCreationDataInvoice result = null;
        try {
            var response = restTemplate.postForEntity(
                    url,
                    request,
                    HoldedCreationDataInvoice.class
            );
            result = response.getBody();
        } catch (HttpStatusCodeException httpException) {
            throw new HoldedNotRespond(
                    url,
                    "",
                    objectToJSON(requestBody),
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
}
