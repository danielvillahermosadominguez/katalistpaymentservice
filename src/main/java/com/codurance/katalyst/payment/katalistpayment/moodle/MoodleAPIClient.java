package com.codurance.katalyst.payment.katalistpayment.moodle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
    public static final String WSTOKEN = "wstoken=";
    public static final String WSFUNCTION = "&wsfunction=";
    public static final String MOODLEWSRESTFORMAT = "&moodlewsrestformat=";
    @Value("${moodle.urlbase}")
    private String URL_BASE;

    @Value("${moodle.token}")
    private String token;
    private String format = "json";

    @Autowired
    private RestTemplate restTemplate;

    private HttpEntity<MultiValueMap<String, String>> createRequest(MultiValueMap<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return  request;
    }
    private String createURL(String moodleWsFunction) {
        return URL_BASE+ WSTOKEN + token + WSFUNCTION +moodleWsFunction+ MOODLEWSRESTFORMAT +format;
    }
    private List<MoodleUserDTO> getUsersForCourse(String courseId) {
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        String url = createURL("core_enrol_get_enrolled_users");
        map.add("courseid", courseId);
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);
        ResponseEntity<MoodleUserDTO[]> response = restTemplate.postForEntity(url, request, MoodleUserDTO[].class);
        return Arrays.stream(response.getBody()).toList();
    }

    public boolean existsAnUserinThisCourse(String courseId, String email) {
        //TODO: We need to review this function - performance issue
        String lowerCaseEmailInput = email.toLowerCase();
        List<MoodleUserDTO> users = getUsersForCourse(courseId);
        List<MoodleUserDTO> filtered = users
                .stream()
                .filter(user -> {
                    String userEmail = user.getEmail();
                    String lowerCaseEmail = userEmail.toLowerCase();
                    return lowerCaseEmail.equals(lowerCaseEmailInput);
                })
                .collect(Collectors.toList());
        return !filtered.isEmpty();
    }

    private MoodleUserDTO getFirst(ResponseEntity<MoodleUserDTO[]> response) {
        List<MoodleUserDTO> resultList = Arrays.stream(response.getBody()).toList();
        if(!resultList.isEmpty()) {
            return resultList.get(0);
        }
        return null;
    }
    public MoodleUserDTO getUserByMail(String email) throws UnsupportedEncodingException {
        ResponseEntity<MoodleUserDTO[]> response = null;
        MoodleUserDTO result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        String url = createURL("core_user_get_users_by_field");
        map.add("field", "email");
        map.add("values[0]",  email);
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);

        try {
            response = restTemplate.postForEntity(url, request, MoodleUserDTO[].class);
            result = getFirst(response);
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();

        }

        return result;
    }

    public MoodleUserDTO createAnUser(String name, String surname, String email) throws UnsupportedEncodingException {
        String url = URL_BASE + WSTOKEN + token + "&wsfunction=core_user_create_users" + MOODLEWSRESTFORMAT + format;
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
        String url = URL_BASE + WSTOKEN + token + "&wsfunction=enrol_manual_enrol_users" + MOODLEWSRESTFORMAT + format;
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
        String url = URL_BASE+ WSTOKEN + token + "&wsfunction=core_course_get_courses"+ MOODLEWSRESTFORMAT +format;
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
