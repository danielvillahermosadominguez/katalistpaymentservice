package com.codurance.katalyst.payment.katalistpayment.holded;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HoldedAPIClient {
    private static String URL_BASE = "https://api.holded.com/api/";
    private String apyKey = "2a1b27283346f020db6c33889f2eaeae";
    @Autowired
    private RestTemplate restTemplate;

    public HoldedContactDTO getContact(String email) {
        HoldedContactDTO result = null;
        String url = URL_BASE + "invoicing/v1/contacts";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        headers.add("key", apyKey);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("email", email);
        Map<String, String> vars = new HashMap<>();
        vars.put("email", email);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<HoldedContactDTO[]> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, request, HoldedContactDTO[].class, vars);
            List<HoldedContactDTO> contacts = Arrays.stream(response.getBody()).toList();
            //response = restTemplate.postForEntity(url, request, String.class);
            if (!contacts.isEmpty()) {
                result = contacts.get(0);
            }
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }


        return result;
    }

    public HoldedContactDTO createContact(String name, String surname, String email, String company, String dnicif) {
        return null;
    }

    public HoldedInvoiceDTO createInvoice(HoldedContactDTO contact, String concept, String description, int amount, double price) {
        return null;
    }
}
