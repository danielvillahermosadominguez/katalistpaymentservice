package com.codurance.katalyst.payment.katalistpayment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class MoodleAPIClient {
    private static String URL_BASE = "https://codurance.moodlecloud.com/webservice/rest/server.php?";
    private String token = "3ef9b832fd76a6cac0c67c053d0a38d5";
    //private static String URL_BASE = "https://exampleforcodurance.moodlecloud.com/webservice/rest/server.php?";
    //private String token = "672349878c7a8d767a16bd5523be32b6";
    private String format = "json";

    @Autowired
    private RestTemplate restTemplate;

    private List<MoodleUserDTO> getUsersForCourse(String courseId) {
        String url = URL_BASE+ "wstoken=" + token + "&wsfunction=core_enrol_get_enrolled_users"+"&moodlewsrestformat="+format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("courseid", courseId);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<MoodleUserDTO[]> response = restTemplate.postForEntity(url, request, MoodleUserDTO[].class);


        return Arrays.stream(response.getBody()).toList();
    }

    public boolean existsAnUserinThisCourse(String courseId, String email) {
        List<MoodleUserDTO> users = getUsersForCourse(courseId);
        List<MoodleUserDTO> filtered = users.stream().filter(c -> c.getEmail().toLowerCase().equals(email.toLowerCase())).collect(Collectors.toList());
        return !filtered.isEmpty();
    }

    public boolean existAnUser(String email) throws UnsupportedEncodingException {
        String url = URL_BASE + "wstoken=" + token + "&wsfunction=core_user_get_users_by_field" + "&moodlewsrestformat=" + format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setContentLanguage(Locale.US);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        String urlParameters ="values[0]=" + URLEncoder.encode("1", "UTF-8");

        map.add("field", "email");
        map.add("values[0]", email);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<MoodleUserDTO[]> response = null;
        try {
            response = restTemplate.postForEntity(url, request, MoodleUserDTO[].class);
            List<MoodleUserDTO> result = Arrays.stream(response.getBody()).toList();
            return !result.isEmpty();
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }

        return false;
    }

    public void createAnUser(String name, String surname, String email) {

    }

    public void subscribeUserToTheCourse(String courseId, String email) {
        String url = URL_BASE + "wstoken=" + token + "&wsfunction=enrol_manual_enrol_users" + "&moodlewsrestformat=" + format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        //Concept test
        String roleId = "5";
        //String userId = "144";
        String userId = "4";
        map.add("enrolments[0][roleid]", roleId);
        map.add("enrolments[0][userid]", userId);
        map.add("enrolments[0][courseid]=", courseId);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(url, request, String.class);
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }
    }
}
