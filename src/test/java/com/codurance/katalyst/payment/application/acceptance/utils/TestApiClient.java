package com.codurance.katalyst.payment.application.acceptance.utils;

import com.codurance.katalyst.payment.application.apirest.dto.Error;
import com.codurance.katalyst.payment.application.model.customer.CustomerData;
import com.codurance.katalyst.payment.application.model.learning.entity.Course;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestApiClient {
    private static final String HTTP_LOCALHOST = "http://localhost:";
    private static final String HEALTHCHECK = "/healthcheck";
    public static final String SUBSCRIPTION = "/subscription";
    private int port = -1;

    private ObjectMapper objectMapper = new ObjectMapper();

    private RestTemplate restTemplate;
    @Autowired
    private TestApiClient() {
        this.restTemplate = new RestTemplate();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ResponseEntity<String> checkItsAlive() {
        return sendRequest(HttpMethod.GET, getUrlBase() + HEALTHCHECK);
    }

    private String getUrlBase() {
        return HTTP_LOCALHOST + this.port;
    }

    private List<Error> errorList = new ArrayList<>();
    private ResponseEntity<String> sendRequest(HttpMethod method, String endpoint) {
        var header = new HttpHeaders();
        header.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(endpoint, method, new HttpEntity<String>(header), String.class);
        } catch (HttpStatusCodeException e) {
            response = ResponseEntity.status(e.getStatusCode()).headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
        return response;
    }

    private ResponseEntity<String> post(String endpoint, String body) {
        HttpHeaders header = new HttpHeaders();
        header.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        header.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<String> response;
        HttpEntity<String> httpEntity = new HttpEntity<String>(body, header);
        try {
            response = restTemplate.exchange(endpoint, HttpMethod.POST, httpEntity, String.class);
        } catch (HttpStatusCodeException e) {
            response = ResponseEntity.status(e.getStatusCode()).headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
        return response;
    }

    private ResponseEntity<String> post(String endPoint, PaymentNotification notification) {
        var header = new HttpHeaders();
        header.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        header.set("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        ResponseEntity<String> response;
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("MethodId", notification.getMethodId().getValue());
        requestBody.add("Order", notification.getOrder());
        requestBody.add("AmountEur", notification.getAmountEur());
        requestBody.add("TpvID", notification.getTpvID());
        requestBody.add("TransactionType", notification.getTransactionType().getValue());
        requestBody.add("Response", notification.getResponse());
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(requestBody, header);

        try {
            response = restTemplate.postForEntity(
                    endPoint,
                    request,
                    String.class
            );
        } catch (HttpStatusCodeException e) {
            response = ResponseEntity.status(e.getStatusCode()).headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
        return response;
    }

    public boolean isInitialized() {
        return this.port != -1;
    }

    public Course getCourse(int selectedCourse) throws JsonProcessingException {
        var response = sendRequest(HttpMethod.GET, getUrlBase() + "/courses/" + selectedCourse);
        if (response.getStatusCode() == HttpStatus.OK) {
            return objectMapper.readValue(response.getBody(), Course.class);
        } else {
            var error = objectMapper.readValue(response.getBody(), Error.class);
            errorList.add(error);
            return null;
        }
    }

    public PaymentStatus paySubscription(CustomerData customData) throws JsonProcessingException {
        var body = objectMapper.writeValueAsString(customData);
        var response = post(getUrlBase() + SUBSCRIPTION, body);
        if (response.getStatusCode() != HttpStatus.OK) {
            var error = objectMapper.readValue(response.getBody(), Error.class);
            this.errorList.add(error);
            return null;
        }

        var paymentStatus = objectMapper.readValue(response.getBody(), PaymentStatus.class);
        return paymentStatus;
    }

    public List<Error> getLastErrors() {
        return errorList;
    }

    public void resetLastErrors() {
        errorList.clear();
    }

    public boolean confirmPayment(PaymentNotification notification) {
        var response = post(getUrlBase() + "/confirmation", notification);
        if (response.getStatusCode() != HttpStatus.OK) {
            return false;
        }
        return true;
    }
}