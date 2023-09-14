package com.codurance.katalyst.payment.application.acceptance.utils;

import com.codurance.katalyst.payment.application.holded.HoldedInvoiceItemDTO;
import com.codurance.katalyst.payment.application.utils.Mail;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.gson.Gson;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Component
public class HoldedServiceFake extends ServiceFake {
    Gson gson = new Gson();
    public String token;
    private final static String URL_BASE = "/api/";
    private MockServer wireMockServer = null;

    private int port;
    private Map<String, Map<String, Object>> contacts = new HashMap();

    public void setPort(int port) {
        this.port = port;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void start() {
        this.wireMockServer = new MockServer(options().port(this.port));
        this.wireMockServer.start();
        this.init();
    }

    private String getParamValue(String link, String paramName) throws URISyntaxException {
        List<NameValuePair> queryParams = new URIBuilder(link).getQueryParams();
        return queryParams.stream()
                .filter(param -> param.getName().equalsIgnoreCase(paramName))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse("");
    }

    private void init() {
        this.wireMockServer.addMockServiceRequestListener(new RequestListener() {
            @Override
            public void requestReceived(Request request, Response response) {
                String url = request.getUrl();
                RequestMethod method = request.getMethod();
                String body = request.getBodyAsString();
                String responseBody = response.getBodyAsString();
                if (
                        method.equals(RequestMethod.POST)
                                && url.equals(URL_BASE + "invoicing/v1/contacts")
                                && response.getStatus() == 200
                ) {

                }
            }
        });
    }

    public void resetAndConfigure() {
        this.reset();
    }

    public void configureGenericStubs() {
        //invoicing/v1/contacts?customId={customId}
    }

    public void reset() {
        this.wireMockServer.getClient().resetRequests();
    }

    public void configureStubsForGetContactByCustomId(Map<String, String> data) throws UnsupportedEncodingException {
        String json = "[]";
        String email = data.get("email");
        Mail mail = new Mail(email);
        String nifCif = data.get("Dni/CIF");
        String customId = URLEncoder.encode(nifCif + mail.getInUnicodeFormat(), "UTF-8");
        String parameters = "?customId=" + customId;
        if (this.contacts.containsKey(customId)) {
            Map<String, Object> contact = this.contacts.get(customId);
            json = gson.toJson(Arrays.asList(contact).toArray());
        }

        stubForGetWithStatusOKAndBodyParameters(parameters, json);
    }

    public void configureStubsForCreateContact(Map<String, String> data) throws UnsupportedEncodingException {
        String email = data.get("email");
        Mail mail = new Mail(email);
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("status", 1);
        bodyMap.put("info", "RANDOM_INFO");
        bodyMap.put("id", "1");
        String json = gson.toJson(bodyMap);

        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        String nifCif = data.get("Dni/CIF");
        String customId = nifCif + mail.getInUnicodeFormat();
        String name = data.get("Name");
        String surname = data.get("Surname");
        String company = data.get("Company");


        requestBodyMap.put("name", name + " " + surname + "(" + company + ")");
        requestBodyMap.put("email", email);
        requestBodyMap.put("type", "client");
        requestBodyMap.put("code", nifCif);
        requestBodyMap.put("CustomId", customId);
        requestBodyMap.put("isperson", "true");

        stubForPostWithStatusOKAndBodyParameters("invoicing/v1/contacts",
                joinParameters(requestBodyMap),
                json);
    }

    private void stubForPostWithStatusOKAndBodyParameters(String function, String requestBody, String responseBody) {
        this.wireMockServer.stubFor(
                post(urlEqualTo(URL_BASE + function))
                        .withRequestBody(containing(requestBody))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withBody(responseBody)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                        )
        );
    }

    public void addContact(Map<String, String> data) throws UnsupportedEncodingException {
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        String email = data.get("email");
        String nifCif = data.get("Dni/CIF");
        Mail mail = new Mail(email);
        String customId = URLEncoder.encode(nifCif + mail.getInUnicodeFormat(), "UTF-8");
        bodyMap.put("id", 1);
        bodyMap.put("customId", mail.getUserName());
        bodyMap.put("email", data.get("email"));
        bodyMap.put("name", data.get("Name"));
        bodyMap.put("email", data.get("email"));
        bodyMap.put("code", nifCif);
        bodyMap.put("type", "");
        this.contacts.put(customId, bodyMap);
    }

    private void stubForGetWithStatusOKAndBodyParameters(String parameters, String responseBody) {
        this.wireMockServer.stubFor(
                get(urlEqualTo(String.format(URL_BASE + "invoicing/v1/contacts%s", parameters)))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())

                                        .withBody(responseBody)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                        )
        );
    }

    public void configureStubsForCreateInvoice(String idInvoice) throws UnsupportedEncodingException {
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("id", idInvoice);
        String json = gson.toJson(bodyMap);

        Map<String, String> requestBodyMap = new LinkedHashMap<>();

        requestBodyMap.put("contactId", "1");
        requestBodyMap.put("desc", "");
        requestBodyMap.put("date", "2323223");
        HoldedInvoiceItemDTO item = new HoldedInvoiceItemDTO("TEST_COURSE", 1, 100);
        List<HoldedInvoiceItemDTO> items = Arrays.asList(item);
        Gson gson = new Gson();
        String jsonArray = gson.toJson(items);
        requestBodyMap.put("items", jsonArray);

        stubForPostWithStatusOKAndBodyParameters("invoicing/v1/documents/invoice",
                joinParameters(requestBodyMap),
                json);
    }
    public void configureStubsForSendInvoice(String email, String invoiceID) throws UnsupportedEncodingException {
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("status", 1);
        bodyMap.put("info", "RANDOM_INFO");
        bodyMap.put("id", "1");
        String json = gson.toJson(bodyMap);

        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        requestBodyMap.put("emails", email);

        stubForPostWithStatusOKAndBodyParameters("invoicing/v1/documents/invoice/"+unicode(invoiceID)+"/send",
                joinParameters(requestBodyMap),
                json);

    }

    public boolean verifySendInvoiceHasBeenCalled() {
        return true;
    }
}
