package com.codurance.katalyst.payment.katalistpayment.holded;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Component
public class HoldedAPIClient {
    private static String URL_BASE = "https://api.holded.com/api/";
    private String apyKey = "2a1b27283346f020db6c33889f2eaeae";
    @Autowired
    private RestTemplate restTemplate;

    public HoldedContactDTO getContact(String email) throws UnsupportedEncodingException {
        HoldedContactDTO result = null;
        String url = URL_BASE + "invoicing/v1/contacts?email={email}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        headers.add("key", apyKey);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        Map<String, String> vars = new HashMap<>();
        vars.put("email", email);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        ResponseEntity<HoldedContactDTO[]> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, request, HoldedContactDTO[].class, vars);
            List<HoldedContactDTO> contacts = Arrays.stream(response.getBody()).toList();
            if (!contacts.isEmpty()) {
                result = contacts.get(0);
            }
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public HoldedContactDTO createContact(String name, String surname, String email, String company, String dnicif) {
        HoldedContactDTO result = null;
        String url = URL_BASE + "invoicing/v1/contacts";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("key", apyKey);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("name", name + " " + surname + "("+ company+ ")");
        map.add("email", email);
        map.add("type", "client");
        map.add("code", dnicif);
        map.add("isperson", "true");


        Map<String, String> vars = new HashMap<>();


        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<HoldedResponse> response = null;
        try {
            response = restTemplate.postForEntity(url, request, HoldedResponse.class);
            result = new HoldedContactDTO();
            result.setId(response.getBody().getId());
            //List<HoldedContactDTO> contacts = Arrays.stream(response.getBody()).toList();
            //response = restTemplate.postForEntity(url, request, String.class);
            //if (!contacts.isEmpty()) {
            //    result = contacts.get(0);
            //}
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }


        return result;
    }

    public HoldedInvoiceDTO createInvoice(HoldedContactDTO contact, String concept, String description, int amount, double price) {
        HoldedInvoiceDTO result = null;
        String url = URL_BASE + "invoicing/v1/documents/invoice";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("key", apyKey);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("contactId", contact.getId());
        map.add("desc",description);
        OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
        Date date = Date.from(utc.toInstant());
        map.add("date", date.toInstant().toEpochMilli() +"");
        map.add("date", date.toInstant().getEpochSecond()+"");
        HoldedInvoiceItemDTO item = new HoldedInvoiceItemDTO();
        item.setName( "Suscription to Katalist");
        //item.setDesc("");
        item.setUnits(1);
        item.setSubtotal(60);
        //item.setSku("prueba SKU");
        List<HoldedInvoiceItemDTO> items = Arrays.asList(item);
        Gson gson = new Gson();
        String jsonArray = gson.toJson(items);
        map.add("items",jsonArray);
        //map.add("items","[{\"name\":\"Suscripción curso KATALIST\", \"units\":1}]");

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
        ResponseEntity<HoldedInvoiceDTO> response = null;
        try {
            response = restTemplate.postForEntity(url, request, HoldedInvoiceDTO.class);
            result = response.getBody();
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }


        return result;
    }


    public void sendInvoice(HoldedInvoiceDTO invoice) {

    }
}
