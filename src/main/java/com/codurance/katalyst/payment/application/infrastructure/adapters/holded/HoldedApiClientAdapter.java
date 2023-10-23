package com.codurance.katalyst.payment.application.infrastructure.adapters.holded;

import com.codurance.katalyst.payment.application.infrastructure.adapters.common.APIClient;
import com.codurance.katalyst.payment.application.infrastructure.adapters.holded.requests.CreateContactRequestBody;
import com.codurance.katalyst.payment.application.infrastructure.adapters.holded.requests.CreateInvoiceItemRequestBody;
import com.codurance.katalyst.payment.application.infrastructure.adapters.holded.requests.CreateInvoiceRequestBody;
import com.codurance.katalyst.payment.application.infrastructure.adapters.holded.requests.HoldedInvoiceStatus;
import com.codurance.katalyst.payment.application.model.ports.clock.Clock;
import com.codurance.katalyst.payment.application.model.ports.email.Email;
import com.codurance.katalyst.payment.application.model.ports.holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedInvoiceInfo;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.model.ports.holded.exceptions.HoldedNotRespond;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
    public static final String EMAILS = "emails";
    public static final String API_KEY_PARAM_NAME = "key";
    @Value("${holded.urlbase}")
    private String URL_BASE;

    @Value("${holded.apikey}")
    private String apyKey;

    @Autowired
    Clock dateService;

    public HoldedApiClientAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setURLBase(String urlBase) {
        this.URL_BASE = urlBase;
    }

    public void setApiKey(String apiKey) {
        this.apyKey = apiKey;
    }

    public void setDateService(Clock dateService) {
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

        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    HoldedContact[].class,
                    uriVariables);

            return getFirst(response);
        }  catch (HttpStatusCodeException httpException) {
            throw new HoldedNotRespond(
                    url,
                    uriVariables.toString(),
                    "",
                    httpException.getMessage()
            );
        }
    }

    public HoldedContact createContact(HoldedContact contact) throws HoldedNotRespond {
        var url = generateEndPoint("invoicing/v1/contacts");
        var requestBody = new CreateContactRequestBody(contact);
        var request = createRequestEntity(
                requestBody,
                MediaType.APPLICATION_JSON_VALUE
        );

        try {
            var response = restTemplate.postForEntity(
                    url,
                    request,
                    HoldedStatus.class
            );

            var body = response.getBody();
            if (body.getStatus() == HoldedStatus.OK) {
                return getContactByCustomId(contact.getCustomId());
            }
        }  catch (HttpStatusCodeException httpException) {
            throw new HoldedNotRespond(
                    url,
                    "",
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }
        return null;
    }

    public HoldedInvoiceInfo createInvoice(HoldedContact contact, String concept, String description, int amount, double price) throws HoldedNotRespond {
        var url = generateEndPoint("invoicing/v1/documents/invoice");
        var instant = dateService.getInstant();
        var invoiceItem = new CreateInvoiceItemRequestBody(
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

        try {
            var response = restTemplate.postForEntity(
                    url,
                    request,
                    HoldedInvoiceStatus.class
            );
            // ----- Holded API Documentation inconsistency -------------------
            // Be careful with the documentation of Holded:https://developers.holded.com/reference/create-document-1
            // This API method don't send as response a HoldedInvoice. Instead of this:
            //{
            //    "status": 1,
            //    "id": "6523fbb4056b14a8e70b3cef",
            //    "invoiceNum": "F230017",
            //    "contactId": "6523fa44ca9b5aa0880b5a41"
            //}
            // We decide not to expose this Request and send an invoice info
            var status = response.getBody();
            if (status.getStatus() == HoldedInvoiceStatus.OK) {
                return new HoldedInvoiceInfo(status.getId());
            }
        } catch (HttpStatusCodeException httpException) {
            throw new HoldedNotRespond(
                    url,
                    "",
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }

        return null;
    }

    public HoldedStatus sendInvoice(HoldedInvoiceInfo invoice, List<Email> emails) throws HoldedNotRespond {
        var strEmails = Email.getRecipients(emails);
        var url = generateEndPoint("invoicing/v1/documents/invoice/" + invoice.getId() + "/send");

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(EMAILS, strEmails);

        var request = createRequest(
                requestBody,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE
        );

        try {
            var response = restTemplate.postForEntity(
                    url,
                    request,
                    HoldedStatus.class
            );
            return response.getBody();
        } catch (HttpStatusCodeException httpException) {
            throw new HoldedNotRespond(
                    url,
                    "",
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }
    }
}
