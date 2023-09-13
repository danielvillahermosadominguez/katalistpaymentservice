package com.codurance.katalyst.payment.application.acceptance.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Component
public class MoodleServiceFake {

    private String URL_BASE = "/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json";
    private WireMockServer wireMockServer = null;

    private List<Object> courses = new ArrayList<>();

    private Map<Integer, List<Object>> studentsPerCourse = new HashMap<>();
    private int port;

    public void setPort(int port) {
        this.port = port;
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

    public void configureStubs() throws UnsupportedEncodingException {
        Gson gson = new Gson();
        String json = gson.toJson(courses.toArray());

        WireMock.stubFor(
                post(
                        urlEqualTo(String.format(URL_BASE, "AN_INVENTED_TOKEN", "core_course_get_courses"))
                )
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody(json)
                                        .withHeader("Content-Type", "application/json")
                        )
        );


        json = gson.toJson(Arrays.asList().toArray());
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("courseid", "9");

        WireMock.stubFor(
                post(
                        urlEqualTo(String.format(URL_BASE, "AN_INVENTED_TOKEN", "core_enrol_get_enrolled_users"))
                )
                        .withRequestBody(containing("courseid=9"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody(json)
                                        .withHeader("Content-Type", "application/json")
                        )
        );

        bodyMap.clear();
        bodyMap.put("id", 1);
        bodyMap.put("username", "John");
        bodyMap.put("email", "john@example.com");
        json = gson.toJson(Arrays.asList(bodyMap).toArray());
        String userName = unicode("users[0][username]") + "=" + unicode("john");
        String createPassword = unicode("users[0][createpassword]") + "=" + unicode("1");
        String email = unicode("users[0][email]") + "=" + unicode("john@example.com");
        String firstName = unicode("users[0][firstname]") + "=" + unicode("John");
        String lastName = unicode("users[0][lastname]") + "=" + unicode("Doe");

        WireMock.stubFor(
                post(
                        urlEqualTo(String.format(URL_BASE, "AN_INVENTED_TOKEN", "core_user_create_users"))
                )

                        .withRequestBody(containing(userName + "&" + createPassword + "&" + email + "&" + firstName + "&" + lastName))

                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody(json)
                                        .withHeader("Content-Type", "application/json")
                        )
        );
        String rolId = unicode("enrolments[0][roleid]") + "=" + unicode("5");
        String userId = unicode("enrolments[0][userid]") + "=" + unicode("1");
        String courseId = unicode("enrolments[0][courseid]") + "=" + unicode("0");

        WireMock.stubFor(
                post(
                        urlEqualTo(String.format(URL_BASE, "AN_INVENTED_TOKEN", "enrol_manual_enrol_users"))
                )

                        .withRequestBody(containing(rolId + "&" + userId + "&" + courseId))

                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody(json)
                                        .withHeader("Content-Type", "application/json")
                        )
        );
    }

    private String unicode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8");
    }

    public void reset() {
        this.wireMockServer.resetRequests();
        courses.clear();
    }
}
