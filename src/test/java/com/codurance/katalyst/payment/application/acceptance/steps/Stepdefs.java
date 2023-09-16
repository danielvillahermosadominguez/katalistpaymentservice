package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.utils.MoodleApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestApiClient;
import com.codurance.katalyst.payment.application.courses.Course;
import com.codurance.katalyst.payment.application.integration.HoldedWireMockHelper;
import com.codurance.katalyst.payment.application.moodle.MoodleCourseDTO;
import com.codurance.katalyst.payment.application.moodle.MoodleUserDTO;
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
    public static final int WIREMOCK_HOLDED_PORT = 9001;
    public static final int FIXTURE_PRICE = 100;
    public static final String FIXTURE_DISPLAY_NAME = "TEST_COURSE";
    public static MoodleCourseDTO FIXTURE_COURSE = null;

    public static MoodleUserDTO FIXTURE_USER = null;

    private int selecteCourse;
    int subscritionOutputCode = -1;
    int invoiceOutputCode = -1;
    Map<String, String> data = null;
    boolean newUser = true;
    @LocalServerPort
    int randomServerPort;

    @Value("${holded.apikey}")
    private String holdedApiKey;

    @Autowired
    private TestApiClient apiClient;

    @Autowired
    MoodleApiClientFake moodleApiClient;

    @Autowired
    private HoldedWireMockHelper holdedService;

    @Before
    public void beforeEachScenario() {
        if (!apiClient.isInitialized()) {
            this.apiClient.setPort(randomServerPort);
            holdedService.setPort(WIREMOCK_HOLDED_PORT);
            holdedService.setToken(holdedApiKey);
            holdedService.start();

        }
        ResponseEntity<String> response = this.apiClient.checkItsAlive();

        if (!response.getBody().equals("OK! Working")) {
            fail();
        }

        moodleApiClient.reset();
        holdedService.resetAndConfigure();
        FIXTURE_COURSE = moodleApiClient.addCourse(FIXTURE_DISPLAY_NAME, FIXTURE_PRICE);
        holdedService.configureGenericStubs();
    }

    @Given("A company who has choosen a course")
    public void a_company_who_has_choosen_a_course() {

    }

    @Given("An customer who has choosen a course")
    public void an_customer_who_has_choosen_a_course() {
        this.selecteCourse = FIXTURE_COURSE.getId();
        Course course = this.apiClient.getCourse(this.selecteCourse);
        assertThat(course).isNotNull();
    }

    @Given("the user has filled the following data")
    public void the_user_has_filled_the_following_data(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        assertThat(rows.size()).isEqualTo(1);
        data = rows.get(0);
        subscritionOutputCode = apiClient.subscribe(this.selecteCourse, data);
        assertThat(subscritionOutputCode).isEqualTo(1);
    }

    @Given("he\\/she has been subscribed to other courses in the past with the following data")
    public void he_she_has_been_subscribed_to_other_courses_in_the_past_with_the_following_data(DataTable dataTable) throws UnsupportedEncodingException {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        assertThat(rows.size()).isEqualTo(1);
        data = rows.get(0);
        holdedService.addContact(data);
        String name = data.get("Name");
        String surname = data.get("Surname");
        String email = data.get("email");
        FIXTURE_USER = moodleApiClient.createUser(name, surname, email);
        moodleApiClient.enroleToTheCourse(FIXTURE_COURSE, FIXTURE_USER);
    }

    @When("the user request the subscription")
    public void the_user_request_the_subscription() {
        subscritionOutputCode = apiClient.subscribe(this.selecteCourse, data);
    }

    @Then("the user is informed he\\/she is already subscribed to this course")
    public void the_user_is_informed_he_she_is_already_subscribed_to_this_course() {

    }

    @When("the user pay the subscription")
    public void the_user_pay_the_subscription() throws UnsupportedEncodingException {
        this.holdedService.configureStubsForGetContactByCustomId(data);
        if (newUser) {
            this.holdedService.configureStubsForCreateContact(data);
        }
        this.holdedService.configureStubsForCreateInvoice("IDINVOICE");
        this.holdedService.configureStubsForSendInvoice(data.get("email"),"IDINVOICE");
        this.invoiceOutputCode = this.apiClient.payment(this.selecteCourse, data);
    }

    @Then("the subscription is successful")
    public void the_subscription_is_successful() {
        assertThat(subscritionOutputCode).isEqualTo(1);
        assertThat(invoiceOutputCode).isEqualTo(1);
    }

    @Then("the user has received an invoice")
    public void the_user_has_received_an_invoice() throws UnsupportedEncodingException {
      this.holdedService.verifySendInvoiceHasBeenCalled(data.get("email"),"IDINVOICE");

    }
    @Then("the user has received the access to the platform")
    public void the_user_has_received_the_access_to_the_platform() {
        assertThat(subscritionOutputCode).isEqualTo(1);
    }

    @When("the user request the subscription to the course")
    public void the_user_request_the_subscription_to_the_course() {
        // Write code here that turns the phrase above into concrete actions

    }
}
