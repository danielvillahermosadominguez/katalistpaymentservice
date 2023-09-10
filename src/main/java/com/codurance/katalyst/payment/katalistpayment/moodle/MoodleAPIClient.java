package com.codurance.katalyst.payment.katalistpayment.moodle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class MoodleAPIClient {
    // We need to create
    //private static String URL_BASE = "https://codurance.moodlecloud.com/webservice/rest/server.php?";
    //private String token = "3ef9b832fd76a6cac0c67c053d0a38d5";

    @Value("${moodle.urlbase}")
    private String URL_BASE;

    @Value("${moodle.token}")
    private String token;
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

    public MoodleUserDTO getUserByMail(String email) throws UnsupportedEncodingException {
        MoodleUserDTO result = null;
        String url = URL_BASE + "wstoken=" + token + "&wsfunction=core_user_get_users_by_field" + "&moodlewsrestformat=" + format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setContentLanguage(Locale.US);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("field", "email");
        map.add("values[0]",  email);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<MoodleUserDTO[]> response = null;
        try {
            response = restTemplate.postForEntity(url, request, MoodleUserDTO[].class);
            List<MoodleUserDTO> resultList = Arrays.stream(response.getBody()).toList();
            if(!resultList.isEmpty()) {
                result = resultList.get(0);
            }
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public MoodleUserDTO createAnUser(String name, String surname, String email) throws UnsupportedEncodingException {
        String url = URL_BASE + "wstoken=" + token + "&wsfunction=core_user_create_users" + "&moodlewsrestformat=" + format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setContentLanguage(Locale.US);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("users[0][username]", generateUserNameBasedOn(email));
        map.add("users[0][createpassword]", "1");
        //map.add("users[0][password]", "@Codurance2023$");
        map.add("users[0][email]",  email);
        map.add("users[0][firstname]",name);
        map.add("users[0][lastname]", surname);

        HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<MoodleUserDTO[]> response = null;

        try {
            response = restTemplate.postForEntity(url, request, MoodleUserDTO[].class);
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }
        List<MoodleUserDTO> result = Arrays.stream(response.getBody()).toList();
        //We need to encapusalte in our own exception
        return result.get(0);
    }

    private String generateUserNameBasedOn(String mail) {
        String finalOutput = "";
        String arrayOfStr[] = mail.split("@");
        if (arrayOfStr.length == 2) {
            finalOutput = arrayOfStr[0];
        }
        finalOutput = finalOutput.replaceAll("[!#$%&'*+-/=?]","");

        return finalOutput;
    }

    public void subscribeUserToTheCourse(MoodleCourseDTO course, MoodleUserDTO user) {
        String url = URL_BASE + "wstoken=" + token + "&wsfunction=enrol_manual_enrol_users" + "&moodlewsrestformat=" + format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        //Concept test
        String roleId = "5";
        String userId = user.getId();
        map.add("enrolments[0][roleid]", roleId);
        map.add("enrolments[0][userid]", userId);
        map.add("enrolments[0][courseid]=", course.getId()+"");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(url, request, String.class);
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
        }
    }

    public MoodleCourseDTO getCourse(String courseId) {
        String url = URL_BASE+ "wstoken=" + token + "&wsfunction=core_course_get_courses"+"&moodlewsrestformat="+format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("options[ids][0]", courseId);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        MoodleCourseDTO result = null;
        ResponseEntity<MoodleCourseDTO[]> response = null;
        try {
            response = restTemplate.postForEntity(url, request, MoodleCourseDTO[].class);

            List<MoodleCourseDTO> courses = Arrays.stream(response.getBody()).toList();
            if(!courses.isEmpty()) {
                result = courses.get(0);
            }
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            //Habria que controlar
        }

        return result;
    }
}
