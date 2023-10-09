package com.codurance.katalyst.payment.application.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class APIClient {
    @Autowired
    protected RestTemplate restTemplate;

    private Map<String, String> headersParams = new HashMap<>();

    protected <T> HttpEntity<MultiValueMap<String, T>> createRequest(MultiValueMap<String, T> requestBody, String mediaType) {
        var headers = new HttpHeaders();
        getHeaderParameter(headers);
        headers.setContentType(MediaType.valueOf(mediaType));
        HttpEntity<MultiValueMap<String, T>> request = requestBody == null
                ? new HttpEntity<>(headers)
                : new HttpEntity<>(requestBody, headers);
        return request;
    }

    protected <T> HttpEntity<T> createRequestEntity(T requestBody,  String mediaType) {
        var headers = new HttpHeaders();
        getHeaderParameter(headers);
        headers.setContentType(MediaType.valueOf(mediaType));
        var request = new HttpEntity<T>(requestBody, headers);
        return request;
    }

    protected HttpEntity<String> createRequestString(String requestBody, String mediaType) {
        var headers = new HttpHeaders();
        getHeaderParameter(headers);
        headers.setContentType(MediaType.valueOf(mediaType));
        HttpEntity<String> request = requestBody == null
                ? new HttpEntity<>(headers)
                : new HttpEntity<>(requestBody, headers);
        return request;
    }
    protected void getHeaderParameter(HttpHeaders headers) {

    }

    protected  <T> T getFirst(ResponseEntity<T[]> response) {
        List<T> resultList = Arrays.stream(response.getBody()).toList();
        if(resultList.isEmpty()) {
            return null;
        }

        return resultList.get(0);
    }

    protected  <T> String objectToJSON(T object) {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
