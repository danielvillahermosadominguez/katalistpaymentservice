package com.codurance.katalyst.payment.application.acceptance.utils;

import com.codurance.katalyst.payment.application.api.Course;
import com.codurance.katalyst.payment.application.api.Error;
import com.codurance.katalyst.payment.application.api.PotentialCustomerData;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
@Component
public class TestApiClient {
    private static final String HTTP_LOCALHOST = "http://localhost:";
    private static final String HEALTHCHECK = "/healthcheck";
    public static final int SUCCESS_CODE = 0;
    private int port = -1;

    private Gson gson = new Gson();

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

    private ResponseEntity<String> sendRequest(HttpMethod method, String endpoint) {
        HttpHeaders header = new HttpHeaders();
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

    public boolean isInitialized() {
        return this.port != -1;
    }

    public Course getCourse(int selectedCourse) {
        ResponseEntity<String> response = sendRequest(HttpMethod.GET, getUrlBase() + "/courses/" + selectedCourse);
        if (response.getStatusCode() == HttpStatus.OK) {
            return gson.fromJson(response.getBody(), Course.class);
        } else {
            return null;
        }
    }

    public int freeSubscription(int courseId, Map<String, String> data) {
        Map<String, String> potentialCustomer = new HashMap<>();
        potentialCustomer.put("paymentMethod", "");
        potentialCustomer.put("courseId", courseId + "");
        potentialCustomer.put("email", data.get("email"));
        potentialCustomer.put("name", data.get("Name"));
        potentialCustomer.put("surname", data.get("Surname"));
        potentialCustomer.put("company", data.get("Company"));
        potentialCustomer.put("dnicif", data.get("Dni/CIF"));

        String body = gson.toJson(potentialCustomer);
        ResponseEntity<String> response = post(getUrlBase() + "/freesubscription", body);
        if (response.getStatusCode() != HttpStatus.OK) {
            Error error = gson.fromJson(response.getBody(), Error.class);
            return error.getCode();
        }
        return SUCCESS_CODE;
    }

    public int invoicing(int courseId, Map<String, String> data) {
        Map<String, String> potentialCustomer = new HashMap<>();
        potentialCustomer.put("paymentMethod", "");
        potentialCustomer.put("courseId", courseId + "");
        potentialCustomer.put("email", data.get("email"));
        potentialCustomer.put("name", data.get("Name"));
        potentialCustomer.put("surname", data.get("Surname"));
        potentialCustomer.put("company", data.get("Company"));
        potentialCustomer.put("dnicif", data.get("Dni/CIF"));

        var body = gson.toJson(potentialCustomer);
        var response = post(getUrlBase() + "/invoicing", body);
        if (response.getStatusCode() != HttpStatus.OK) {
            Error error = gson.fromJson(response.getBody(), Error.class);
            return error.getCode();
        }
        return SUCCESS_CODE;
    }

    public int subscription(PotentialCustomerData customData) {

        var body = gson.toJson(customData);
        var response = post(getUrlBase() + "/subscription", body);
        if (response.getStatusCode() != HttpStatus.OK) {
            Error error = gson.fromJson(response.getBody(), Error.class);
            return error.getCode();
        }
        return SUCCESS_CODE;
    }
}