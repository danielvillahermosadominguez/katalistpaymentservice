package com.codurance.katalyst.payment.application.acceptance.utils;

import com.codurance.katalyst.payment.application.utils.Mail;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.gson.Gson;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Component
public class MoodleServiceFake extends ServiceFake {
    public static final String STUDENT_ROL_ID = "5";
    Gson gson = new Gson();
    public String token;
    private final static String URL_BASE = "/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json";
    private MockServer wireMockServer = null;
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

    public void resetAndConfigure() {
        this.reset();
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

    private void configureStubsForGetCoursesByEmail() {
        String json = gson.toJson(Arrays.asList());
        stubForPostWithStatusOK("core_user_get_users_by_field", json);
    }

    private void configureStubsForGetEnroledUsers() {
        String json = gson.toJson(Arrays.asList().toArray());
        stubForPostWithStatusOK("core_enrol_get_enrolled_users", json);
    }

    public void configureStubsForCreateUser(Map<String, String> data) throws UnsupportedEncodingException {
        Mail mail = new Mail(data.get("email"));
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("id", 1);
        bodyMap.put("username", mail.getUserName());
        bodyMap.put("email", data.get("email"));
        String json = gson.toJson(Arrays.asList(bodyMap).toArray());

        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        requestBodyMap.put("users[0][username]", mail.getUserName());
        requestBodyMap.put("users[0][createpassword]", "1");
        requestBodyMap.put("users[0][email]", data.get("email"));
        requestBodyMap.put("users[0][firstname]", data.get("Name"));
        requestBodyMap.put("users[0][lastname]", data.get("Surname"));

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

    public void configureGenericStubs() throws UnsupportedEncodingException {
        configureStubsForGetCourses();
        configureStubsForGetEnroledUsers();
        configureStubsForGetCoursesByEmail();
        configureStubsForEnroleUser();
    }

    public void reset() {
        this.wireMockServer.resetRequests();
        courses.clear();
    }

    public void stop() {
        this.wireMockServer.stop();
    }
}
