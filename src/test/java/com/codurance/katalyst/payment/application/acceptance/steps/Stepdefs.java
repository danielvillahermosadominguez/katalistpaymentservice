package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.utils.MoodleServiceFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestApiClient;
import com.codurance.katalyst.payment.application.courses.Course;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class Stepdefs {
    public static final int MOODLE_PORT = 9000;
    private int selecteCourse;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private TestApiClient apiClient;

    @Autowired
    private MoodleServiceFake moodleService;

    @Before
    public void beforeEachScenario() throws JsonProcessingException {
        if(!apiClient.isInitialized()) {
            this.apiClient.setPort(randomServerPort);
            moodleService.setPort(MOODLE_PORT);
            moodleService.start();
        }
        ResponseEntity<String> response = this.apiClient.checkItsAlive();

        if(!response.getBody().equals("OK! Working")) {
            fail();
        }
        moodleService.resetAndConfigure();
        moodleService.addCourse(9,"TEST_COURSE", 100);
        moodleService.configureStubs();
    }
    @Given("A company who has choosen a course")
    public void a_company_who_has_choosen_a_course() {
        this.selecteCourse = 9;

        Course course = this.apiClient.getCourse(this.selecteCourse);
        assertThat(course).isNotNull();
    }

    @Given("the user has filled the following data")
    public void the_user_has_filled_the_following_data(io.cucumber.datatable.DataTable dataTable) {
        // Write code here that turns the phrase above into concrete actions
        // For automatic transformation, change DataTable to one of
        // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or
        // Map<K, List<V>>. E,K,V must be a String, Integer, Float,
        // Double, Byte, Short, Long, BigInteger or BigDecimal.
        //
        // For other transformations you can register a DataTableType.

    }

    @When("the user request the subscription")
    public void the_user_request_the_subscription() {

    }
    @Then("the user is informed he\\/she is already subscribed to this course")
    public void the_user_is_informed_he_she_is_already_subscribed_to_this_course() {

    }

    @When("the user pay the subscription")
    public void the_user_pay_the_subscription() {
        // Write code here that turns the phrase above into concrete actions

    }
    @Then("the subscription is successful")
    public void the_subscription_is_successful() {
        // Write code here that turns the phrase above into concrete actions

    }
    @Then("the user has received an invoice")
    public void the_user_has_received_an_invoice() {
        // Write code here that turns the phrase above into concrete actions

    }
    @Then("the user has received the access to the platform")
    public void the_user_has_received_the_access_to_the_platform() {
        // Write code here that turns the phrase above into concrete actions

    }

    @Given("An customer who has choosen a course")
    public void an_customer_who_has_choosen_a_course() {
        // Write code here that turns the phrase above into concrete actions

    }
    @When("the user request the subscription to the course")
    public void the_user_request_the_subscription_to_the_course() {
        // Write code here that turns the phrase above into concrete actions

    }
}
