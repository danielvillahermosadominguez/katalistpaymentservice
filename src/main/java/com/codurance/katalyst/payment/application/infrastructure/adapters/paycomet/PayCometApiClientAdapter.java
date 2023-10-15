package com.codurance.katalyst.payment.application.infrastructure.adapters.paycomet;

import com.codurance.katalyst.payment.application.infrastructure.adapters.common.APIClient;
import com.codurance.katalyst.payment.application.infrastructure.adapters.paycomet.dto.PaymentBody;
import com.codurance.katalyst.payment.application.infrastructure.adapters.paycomet.dto.PaymentParams;
import com.codurance.katalyst.payment.application.model.ports.paycomet.PayCometApiClient;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentOrder;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.model.ports.paycomet.exception.PayCometNotRespond;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;

@Component
public class PayCometApiClientAdapter extends APIClient implements PayCometApiClient {

    public static final String API_KEY_PARAM_NAME = "PAYCOMET-API-TOKEN";
    @Value("${paycomet.urlbase}")
    private String URL_BASE;

    @Value("${paycomet.apikey}")
    private String apyKey;

    @Value("${paycomet.terminal}")
    private int terminal;

    public PayCometApiClientAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void getHeaderParameter(HttpHeaders headers) {
        headers.add(API_KEY_PARAM_NAME, apyKey);
    }

    public String generateEndPoint(String function) {
        return URL_BASE + function;
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    public CreatedUser createUser(String jetToken) throws PayCometNotRespond {
        CreatedUser result = null;
        var url = generateEndPoint("/v1/cards");
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("terminal", terminal);
        requestBody.add("jetToken", jetToken);

        var request = createRequest(requestBody, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        ResponseEntity<CreatedUser> response = null;
        try {
            response = restTemplate.postForEntity(url, request, CreatedUser.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                result = response.getBody();
            }
        } catch (HttpStatusCodeException httpException) {
            throw new PayCometNotRespond(
                    url,
                    "",
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }

        return result;
    }

    @Override
    public PaymentStatus authorizePayment(PaymentOrder paymentOrder) throws PayCometNotRespond {
        PaymentStatus result = null;
        String paymentBody = "";
        var url = generateEndPoint("/v1/payments");
        try {
            paymentBody = createPaymentBody(paymentOrder);
            var request = createRequestString(
                    paymentBody,
                    MediaType.APPLICATION_JSON_VALUE
            );
            var response = restTemplate.postForEntity(
                    url,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                result = objectMapper.readValue(response.getBody(), PaymentStatus.class);
            }
        } catch (JsonProcessingException | HttpStatusCodeException httpException) {
            throw new PayCometNotRespond(
                    url,
                    "",
                    paymentBody,
                    httpException.getMessage()
            );
        }

        return result;
    }

    private String createPaymentBody(PaymentOrder paymentOrder) throws JsonProcessingException {
        var requestBody = new PaymentBody();
        var requestParameters = new PaymentParams();
        var amount = convertIntoStringFormat(paymentOrder.getAmount());
        requestBody.setPayment(requestParameters);
        requestParameters.setTerminal(terminal);
        requestParameters.setOriginalIp(paymentOrder.getOriginalIp());
        requestParameters.setAmount(amount);
        requestParameters.setIdUser(paymentOrder.getIdUser());
        requestParameters.setOrder(paymentOrder.getOrder());
        requestParameters.setTokenUser(paymentOrder.getTokenUser());
        return objectMapper.writeValueAsString(requestBody);
    }

    private String convertIntoStringFormat(double amount) {
        var strAmount = new DecimalFormat("#.00#").format(amount);
        strAmount = strAmount.replace(".", "");
        strAmount = strAmount.replace(",", "");
        return strAmount;
    }

    public void setURLBase(String urlBase) {
        this.URL_BASE = urlBase;
    }

    public void setAPIKey(String apiKey) {
        this.apyKey = apiKey;
    }

    public void setTerminal(int terminal) {
        this.terminal = terminal;
    }
}