package com.codurance.katalyst.payment.katalistpayment.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;


public class APIClient {
    @Autowired
    protected RestTemplate restTemplate;

    private MultiValueMap<String, String> headersParams = new LinkedMultiValueMap<>();
    private String mediaType;
    protected HttpEntity<MultiValueMap<String, String>> createRequest(MultiValueMap<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(mediaType));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return  request;
    }

    protected void setHeaderParameter(String name, String value) {
        headersParams.add(name, value);
    }

    protected void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    protected  <T> T getFirst(ResponseEntity<T[]> response) {
        List<T> resultList = Arrays.stream(response.getBody()).toList();
        if(resultList.isEmpty()) {
            return null;
        }

        return resultList.get(0);
    }
}
