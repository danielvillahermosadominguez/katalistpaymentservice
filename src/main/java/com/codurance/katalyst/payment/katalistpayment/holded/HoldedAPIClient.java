package com.codurance.katalyst.payment.katalistpayment.holded;

import com.codurance.katalyst.payment.katalistpayment.utils.Mail;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${holded.urlbase}")
    private String URL_BASE;

    @Value("${holded.apikey}")
    private String apyKey;
    @Autowired
    private RestTemplate restTemplate;

    public HoldedContactDTO getContactByMailOrCustomId(String email, String customId) throws UnsupportedEncodingException {
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
            } else {
                result = getContactByCustomId(customId);
            }
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public HoldedContactDTO getContactByCustomId(String customId) {
        HoldedContactDTO result = null;
        String url = URL_BASE + "invoicing/v1/contacts?customId={customId}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        headers.add("key", apyKey);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        Map<String, String> vars = new HashMap<>();
        vars.put("customId", customId);

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

    public HoldedContactDTO createContact(String name, String surname, String email, String company, String dnicif) throws UnsupportedEncodingException {
        HoldedContactDTO result = null;
        String url = URL_BASE + "invoicing/v1/contacts";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("key", apyKey);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("name", name + " " + surname + "(" + company + ")");
        map.add("email", email);
        map.add("type", "client");
        map.add("code", dnicif);
        map.add("CustomId", createCustomId(dnicif, email));
        map.add("isperson", "true");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<HoldedResponse> response = null;
        try {
            response = restTemplate.postForEntity(url, request, HoldedResponse.class);
            if (response.getBody().getStatus() == 1) {
                String customId = createCustomId(dnicif, email);
                result = getContactByCustomId(customId);
            }
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
        map.add("date", date.toInstant().getEpochSecond()+"");
        HoldedInvoiceItemDTO item = new HoldedInvoiceItemDTO("Suscription to Katalist", 1, 60.99);
        List<HoldedInvoiceItemDTO> items = Arrays.asList(item);
        Gson gson = new Gson();
        String jsonArray = gson.toJson(items);
        map.add("items",jsonArray);

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


    public void sendInvoice(HoldedInvoiceDTO invoice, String emails) {
        String url = URL_BASE + "invoicing/v1/documents/invoice/"+invoice.getId()+"/send";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("key", apyKey);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("emails", emails);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(url, request, String.class);
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }
    }

    public String createCustomId(String nifCif, String email) throws UnsupportedEncodingException {
        Mail mail = new Mail(email);
        return nifCif + mail.getInUnicodeFormat();
    }
}
