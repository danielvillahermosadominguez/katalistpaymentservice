package com.codurance.katalyst.payment.application.acceptance.utils;

import com.codurance.katalyst.payment.application.courses.Course;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
public class TestApiClient {
    private static final String HTTP_LOCALHOST = "http://localhost:";
    private static final String HEALTHCHECK = "/healthcheck";
    private int port = -1;

    private RestTemplate restTemplate;
    private String user;
    private String password;

    @Autowired
    private TestApiClient() {
        this.restTemplate = new RestTemplate();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAuth(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public ResponseEntity<String> checkItsAlive() {
        return sendRequest(HttpMethod.GET, getUrlBase() + HEALTHCHECK);
    }

    private String getUrlBase() {
        return HTTP_LOCALHOST + this.port;
    }

    private ResponseEntity<String> sendRequest(HttpMethod method, String endpoint) {
        HttpHeaders header = new HttpHeaders();
        if(this.user != null && this.password != null) {
            header.setBasicAuth(this.user, this.password);
        }
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

    public boolean isInitialized() {
        return this.port != -1;
    }

    public Course getCourse(int selectedCourse) {
        ResponseEntity<String> response =  sendRequest(HttpMethod.GET, getUrlBase() + "/courses/" + selectedCourse);
        if(response.getStatusCode() == HttpStatus.OK) {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody(), Course.class);
        } else {
            return null;
        }
    }
}