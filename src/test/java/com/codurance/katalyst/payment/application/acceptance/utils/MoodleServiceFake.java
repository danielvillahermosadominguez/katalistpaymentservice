package com.codurance.katalyst.payment.application.acceptance.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.gson.Gson;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@Component
public class MoodleServiceFake {
    public static final String EQUAL_SYMBOL = "=";
    public static final String JOIN_SYMBOL = "&";
    public static final String STUDENT_ROL_ID = "5";
    Gson gson = new Gson();
    public String token;
    private final static String URL_BASE = "/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json";
    private WireMockServer wireMockServer = null;
    private List<Object> courses = new ArrayList<>();

    private Map<Integer, List<Object>> studentsPerCourse = new HashMap<>();
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void start() {
        this.wireMockServer = new WireMockServer(port);
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

    public void resetAndConfigure() {
        this.reset();
        WireMock.configureFor(port);
    }

    public void addCourse(int id, String displayName, double price) {
        Map<String, Object> customField = new HashMap<>();
        customField.put("name", "price");
        customField.put("shortname", "price");
        customField.put("value", price + "");
        List<Map> customFields = Arrays.asList(customField);

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("displayname", displayName);
        map.put("customfields", customFields.toArray());
        
        this.courses.add(map);
    }

    private void configureStubsForGetCourses() {
        String json = gson.toJson(courses.toArray());
        stubForPostWithStatusOK("core_course_get_courses", json);
    }

    private void configureStubsForGetEnroledUsers() {
        String json = gson.toJson(Arrays.asList().toArray());
        stubForPostWithStatusOK("core_enrol_get_enrolled_users", json);
    }

    private void configureStubsForCreateUser() throws UnsupportedEncodingException {
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("id", 1);
        bodyMap.put("username", "John");
        bodyMap.put("email", "john@example.com");
        String json = gson.toJson(Arrays.asList(bodyMap).toArray());

        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        requestBodyMap.put("users[0][username]", "john");
        requestBodyMap.put("users[0][createpassword]", "1");
        requestBodyMap.put("users[0][email]", "john@example.com");
        requestBodyMap.put("users[0][firstname]", "John");
        requestBodyMap.put("users[0][lastname]", "Doe");

        stubForPostWithStatusOKAndBodyParameters("core_user_create_users",
                joinParameters(requestBodyMap),
                json);
    }

    private void configureStubsForEnroleUser() throws UnsupportedEncodingException {
        String json = gson.toJson(Arrays.asList().toArray());
        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        requestBodyMap.put("enrolments[0][roleid]", STUDENT_ROL_ID);
        requestBodyMap.put("enrolments[0][userid]", "1");
        requestBodyMap.put("enrolments[0][courseid]", "9");

        stubForPostWithStatusOKAndBodyParameters("enrol_manual_enrol_users",
                joinParameters(requestBodyMap),
                json);
    }

    private void stubForPostWithStatusOK(String function, String responseBody) {
        WireMock.stubFor(
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

    private void stubForPostWithStatusOKAndBodyParameters(String function, String requestBody, String responseBody) {
        WireMock.stubFor(
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

    public void configureStubs() throws UnsupportedEncodingException {
        configureStubsForGetCourses();
        configureStubsForGetEnroledUsers();
        configureStubsForCreateUser();
        configureStubsForEnroleUser();
    }

    private String joinParameters(Map<String, String> requestBodyMap) throws UnsupportedEncodingException {
        String requestBody = "";

        for (Map.Entry<String, String> parameter : requestBodyMap.entrySet()) {
            requestBody += unicode(parameter.getKey()) + EQUAL_SYMBOL + unicode(parameter.getValue()) + JOIN_SYMBOL;
        }
        if (!requestBody.isEmpty()) {
            requestBody = requestBody.substring(0, requestBody.length() - 1);
        }
        return requestBody;
    }

    private String unicode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8");
    }

    public void reset() {
        this.wireMockServer.resetRequests();
        courses.clear();
    }
}
