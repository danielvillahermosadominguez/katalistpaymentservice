package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.utils.HoldedServiceFake;
import com.codurance.katalyst.payment.application.acceptance.utils.MoodleServiceFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestApiClient;
import com.codurance.katalyst.payment.application.courses.Course;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class Stepdefs {
    public static final int WIREMOCK_MOODLE_PORT = 9000;
    public static final int WIREMOCK_HOLDED_PORT = 9001;
    public static final int FIXTURE_PRICE = 100;
    public static final String FIXTURE_DISPLAY_NAME = "TEST_COURSE";
    public static final int FIXTURE_COURSE_ID = 9;

    private int selecteCourse;
    int subscritionOutputCode = -1;
    int invoiceOutputCode = -1;
    Map<String, String> data = null;

    @LocalServerPort
    int randomServerPort;

    @Value("${moodle.token}")
    private String moodleToken;

    @Value("${holded.apikey}")
    private String holdedApiKey;

    @Autowired
    private TestApiClient apiClient;

    @Autowired
    private MoodleServiceFake moodleService;

    @Autowired
    private HoldedServiceFake holdedServiceFake;

    @Before
    public void beforeEachScenario() throws JsonProcessingException, UnsupportedEncodingException {
        if(!apiClient.isInitialized()) {
            this.apiClient.setPort(randomServerPort);
            moodleService.setPort(WIREMOCK_MOODLE_PORT);
            moodleService.setToken(moodleToken);
            moodleService.start();
            holdedServiceFake.setPort(WIREMOCK_HOLDED_PORT);
            holdedServiceFake.setToken(holdedApiKey);
            holdedServiceFake.start();

        }
        ResponseEntity<String> response = this.apiClient.checkItsAlive();

        if(!response.getBody().equals("OK! Working")) {
            fail();
        }

        moodleService.resetAndConfigure();
        holdedServiceFake.resetAndConfigure();

        moodleService.addCourse(FIXTURE_COURSE_ID, FIXTURE_DISPLAY_NAME, FIXTURE_PRICE);
        moodleService.configureGenericStubs();
        holdedServiceFake.configureGenericStubs();
    }

    @Given("A company who has choosen a course")
    public void a_company_who_has_choosen_a_course() {

    }

    @Given("An customer who has choosen a course")
    public void an_customer_who_has_choosen_a_course() {
        this.selecteCourse = FIXTURE_COURSE_ID;
        Course course = this.apiClient.getCourse(this.selecteCourse);
        assertThat(course).isNotNull();
    }

    @Given("the user has filled the following data")
    public void the_user_has_filled_the_following_data(DataTable dataTable) throws UnsupportedEncodingException {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        assertThat(rows.size()).isEqualTo(1);
        data = rows.get(0);
        moodleService.configureStubsForCreateUser(data);
        subscritionOutputCode = apiClient.subscribe(this.selecteCourse, data);
        assertThat(subscritionOutputCode).isEqualTo(1);
    }

    @When("the user request the subscription")
    public void the_user_request_the_subscription() {
        subscritionOutputCode = apiClient.subscribe(this.selecteCourse, data);
    }
    @Then("the user is informed he\\/she is already subscribed to this course")
    public void the_user_is_informed_he_she_is_already_subscribed_to_this_course() {

    }

    @When("the user pay the subscription")
    public void the_user_pay_the_subscription() {
        int invoiceOutputCode = this.apiClient.payment(this.selecteCourse,data);
    }
    @Then("the subscription is successful")
    public void the_subscription_is_successful() {
        assertThat(subscritionOutputCode).isEqualTo(1);
        //assertThat(invoiceOutputCode).isEqualTo(1);
    }
    @Then("the user has received an invoice")
    public void the_user_has_received_an_invoice() {
        // Write code here that turns the phrase above into concrete actions

    }
    @Then("the user has received the access to the platform")
    public void the_user_has_received_the_access_to_the_platform() {
        // Write code here that turns the phrase above into concrete actions

    }

    @When("the user request the subscription to the course")
    public void the_user_request_the_subscription_to_the_course() {
        // Write code here that turns the phrase above into concrete actions

    }
}
