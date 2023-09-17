package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.doubles.HoldedApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.MoodleApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestApiClient;
import com.codurance.katalyst.payment.application.api.Course;
import com.codurance.katalyst.payment.application.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.holded.dto.HoldedInvoice;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.utils.NotValidEMailFormat;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class Stepdefs {
    public static final int FIXTURE_PRICE = 100;
    public static final String FIXTURE_DISPLAY_NAME = "TEST_COURSE";
    public static final int NO_ANSWER = -10;
    public static MoodleCourse FIXTURE_COURSE = null;

    public static MoodleUser FIXTURE_USER = null;

    private int selectedCourse;
    int subscriptionOutputCode = -1;
    int invoiceOutputCode = -1;
    Map<String, String> data = null;
    @LocalServerPort
    int randomServerPort;

    @Autowired
    private TestApiClient apiClient;

    @Autowired
    MoodleApiClientFake moodleApiClient;
    @Autowired
    HoldedApiClientFake holdedApiClient;
    private int subscriptionResult = NO_ANSWER;

    @Before
    public void beforeEachScenario() {
        if (!apiClient.isInitialized()) {
            this.apiClient.setPort(randomServerPort);
        }
        ResponseEntity<String> response = this.apiClient.checkItsAlive();

        if (!response.getBody().equals("OK! Working")) {
            fail();
        }

        moodleApiClient.reset();
        holdedApiClient.reset();

        FIXTURE_COURSE = moodleApiClient.addCourse(FIXTURE_DISPLAY_NAME, FIXTURE_PRICE);
        subscriptionResult = NO_ANSWER;
        FIXTURE_USER = null;
        invoiceOutputCode = NO_ANSWER;
    }


    @Given("An customer who has chosen a course")
    public void an_customer_who_has_chosen_a_course() {
        this.selectedCourse = FIXTURE_COURSE.getId();
        Course course = this.apiClient.getCourse(this.selectedCourse);
        assertThat(course).isNotNull();
    }

    @Given("the user has filled the following data")
    public void the_user_has_filled_the_following_data(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        assertThat(rows.size()).isEqualTo(1);
        data = rows.get(0);
        subscriptionOutputCode = apiClient.subscribe(this.selectedCourse, data);
        assertThat(subscriptionOutputCode).isGreaterThanOrEqualTo(0);
    }

    @Given("he\\/she has been subscribed to other courses in the past with the following data")
    public void he_she_has_been_subscribed_to_other_courses_in_the_past_with_the_following_data(DataTable dataTable) throws UnsupportedEncodingException, NotValidEMailFormat {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        assertThat(rows.size()).isEqualTo(1);
        data = rows.get(0);
        String name = data.get("Name");
        String surname = data.get("Surname");
        var email = new HoldedEmail(data.get("email"));
        String company = data.get("Company");
        String nifcif = data.get("Dni/CIF");
        holdedApiClient.createContact(name, surname, email, company, nifcif);
        FIXTURE_USER = moodleApiClient.createUser(name, surname, email.getValue());
    }

    @Given("he\\/she has been subscribed to the same course in the past with the following data")
    public void he_she_has_been_subscribed_to_the_same_course_in_the_past_with_the_following_data(DataTable dataTable) throws UnsupportedEncodingException, NotValidEMailFormat {
        he_she_has_been_subscribed_to_other_courses_in_the_past_with_the_following_data(dataTable);
        moodleApiClient.enrolToTheCourse(FIXTURE_COURSE, FIXTURE_USER);
    }

    @Then("the user is informed he\\/she is already subscribed to this course")
    public void the_user_is_informed_he_she_is_already_subscribed_to_this_course() {
        assertThat(subscriptionOutputCode).isEqualTo(2);
    }

    @When("the user pay the subscription")
    public void the_user_pay_the_subscription() throws UnsupportedEncodingException {
        this.invoiceOutputCode = this.apiClient.payment(this.selectedCourse, data);
    }

    @Then("the subscription is successful")
    public void the_subscription_is_successful() {
        assertThat(subscriptionOutputCode).isEqualTo(0);
        assertThat(invoiceOutputCode).isEqualTo(1);
    }

    @Then("the user has received an invoice")
    public void the_user_has_received_an_invoice() throws UnsupportedEncodingException {
        String email = data.get("email");
        List<HoldedInvoice> sentInvoices = holdedApiClient.getSentInvoices(email);
        assertThat(sentInvoices.isEmpty()).isFalse();
    }
    @Then("the user has received the access to the platform")
    public void the_user_has_received_the_access_to_the_platform() {
        assertThat(subscriptionOutputCode).isEqualTo(0);
    }

    @When("the user request the subscription to the course")
    public void the_user_request_the_subscription_to_the_course() {
        subscriptionOutputCode = apiClient.subscribe(this.selectedCourse, data);
    }

    @Given("An customer who has chosen a course which is not in the catalog")
    public void an_customer_who_has_chosen_a_course_which_is_not_in_the_catalog() {
        this.selectedCourse = 10;
        Course course = this.apiClient.getCourse(this.selectedCourse);
        assertThat(course).isNull();
    }
    @Then("the subscription is not successful because the course is not in the catalog")
    public void the_subscription_is_not_successful_because_the_course_is_not_in_the_catalog() {
      assertThat(this.subscriptionOutputCode).isEqualTo(1);
    }
}
