package com.codurance.katalyst.payment.application.integration.wiremock;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.gson.Gson;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Component
public class MoodleWireMockServer extends WireMockServerExtension {
    Gson gson = new Gson();
    public String token;
    private final static String URL_BASE = "/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json";
    private MockServer wireMockServer = null;

    private int port;

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

    private void init() {
        this.wireMockServer.addMockServiceRequestListener(new RequestListener() {
            @Override
            public void requestReceived(Request request, Response response) {

            }
        });
    }

    public void stubForPostWithStatusOK(String function, String responseBody) {
        this.wireMockServer.stubFor(
                post(urlEqualTo(String.format(URL_BASE, token, function)))
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

    public void stubForPostWithStatusOKAndBodyParameters(String function, String requestBody, String responseBody) {
        this.wireMockServer.stubFor(
                post(urlEqualTo(String.format(URL_BASE, token, function)))
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

    public void stubForEnroleUsersWithStatusOK(String json, Map<String, String> requestBodyMap) throws UnsupportedEncodingException {
        String requestBody = joinParameters(requestBodyMap);
        stubForPostWithStatusOKAndBodyParameters("enrol_manual_enrol_users",
                requestBody,
                json);
    }

    public void stubForCreateUsersWithStatusOK(String json, Map<String, String> requestBodyMap) throws UnsupportedEncodingException {
        stubForPostWithStatusOKAndBodyParameters("core_user_create_users",
                joinParameters(requestBodyMap),
                json);
    }
    public void stubForGetUsersByFieldWithStatusOk(String json) {
        stubForPostWithStatusOK("core_user_get_users_by_field", json);
    }

    public void stubForGetEnrolledUsersWithStatusOK(String json) {
        stubForPostWithStatusOK("core_enrol_get_enrolled_users", json);
    }

    public void stubForGetCoursesWithStatusOk(String json) {
        stubForPostWithStatusOK("core_course_get_courses", json);
    }
    public void verify(int quantity, String function, String requestBody) {
        this.wireMockServer.verify(quantity,
                postRequestedFor(
                        urlEqualTo(
                                String.format(URL_BASE, token, function
                                )
                        )
                ).withRequestBody(containing(requestBody))
        );
    }

    public void reset() {
        this.wireMockServer.resetRequests();
    }

    public void stop() {
        this.wireMockServer.stop();
    }

    public void verifyEnrolUsersIsCalled(int quantity, Map<String, String> requestBodyMap) throws UnsupportedEncodingException {
        String requestBody = joinParameters(requestBodyMap);
        verify(1, "enrol_manual_enrol_users", requestBody);
    }

    public Map<String, Object> createCourse(int id, String displayName, double price) {
        Map<String, Object> customField = new HashMap<>();
        customField.put("name", "price");
        customField.put("shortname", "price");
        customField.put("value", price + "");
        List<Map> customFields = Arrays.asList(customField);

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("displayname", displayName);
        map.put("customfields", customFields.toArray());

        return map;
    }
}
