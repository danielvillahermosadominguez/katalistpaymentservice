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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class APIClient {
    @Autowired
    protected RestTemplate restTemplate;

    private Map<String, String> headersParams = new HashMap<>();
    private String mediaType;
    protected HttpEntity<MultiValueMap<String, String>> createRequest(MultiValueMap<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        getHeaderParameter(headers);
        headers.setContentType(MediaType.valueOf(mediaType));
        HttpEntity<MultiValueMap<String, String>> request = map == null? new HttpEntity<>(headers) : new HttpEntity<>(map, headers);
        return  request;
    }


    protected void getHeaderParameter(HttpHeaders headers) {

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
