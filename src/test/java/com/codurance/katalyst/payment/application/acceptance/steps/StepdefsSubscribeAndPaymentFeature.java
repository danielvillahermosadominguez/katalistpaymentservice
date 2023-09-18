package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.doubles.HoldedApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.MoodleApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestApiClient;
import com.codurance.katalyst.payment.application.api.PotentialCustomerData;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;


public class StepdefsSubscribeAndPaymentFeature {
    public static final int NO_ANSWER = -10;
    public static MoodleCourse FIXTURE_COURSE = null;

    public static MoodleUser FIXTURE_USER = null;
    private int subscriptionOutputCode = -1;
    private int invoiceOutputCode = -1;
    @LocalServerPort
    int randomServerPort;

    @Autowired
    private TestApiClient apiClient;

    @Autowired
    MoodleApiClientFake moodleApiClient;
    @Autowired
    HoldedApiClientFake holdedApiClient;
    private int subscriptionResult = NO_ANSWER;
    private Map<String, String> userData = null;
    private Map<String, String> creditDebitCardData = null;
    private String payCometUserId = null;
    private String temporalPayCometToken = null;

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
        subscriptionResult = NO_ANSWER;
        invoiceOutputCode = NO_ANSWER;
    }

    @Given("An customer who has chosen the following course the course {string} with a price of {string}")
    public void an_customer_who_has_chosen_the_following_course_the_course_with_a_price_of(String courseName, String priceText) {
        var price = new MoodlePrice(priceText);
        assertThat(price).isNotEqualTo(0);
        FIXTURE_COURSE = moodleApiClient.addCourse(courseName, price.getValue());
        assertThat(FIXTURE_COURSE).isNotNull();
    }

    @Given("the customer has filled the following data")
    public void the_customer_has_filled_the_following_data(DataTable userData) {
        assertThat(FIXTURE_COURSE).isNotNull();
        var rows = userData.asMaps(String.class, String.class);
        assertThat(rows.size()).isEqualTo(1);
        this.userData = rows.get(0);
        subscriptionOutputCode = apiClient.freeSubscription(FIXTURE_COURSE.getId(), this.userData);
        assertThat(subscriptionOutputCode).isGreaterThanOrEqualTo(0);
    }

    @When("the customer request the subscription to the course")
    public void the_customer_request_the_subscription_to_the_course() {
        assertThat(FIXTURE_COURSE).isNotNull();
        assertThat(this.userData).isNotNull();
    }

    @When("the customer pays the subscription with credit\\/debit card")
    public void the_customer_pays_the_subscription_with_credit_debit_card(DataTable creditDebitCardData) {
        var rows = creditDebitCardData.asMaps(String.class, String.class);
        assertThat(rows.size()).isEqualTo(1);
        //Temporal: Here, we need to call to the PayComet - API or a Fake in their case
        this.creditDebitCardData = rows.get(0);
        payCometUserId = "RANDOM_PAYCOMET_USER_ID";
        temporalPayCometToken = "RANDOM_TEMPORAL_PAYCOMET_TOKEN";
        var customData = convertToCustomData();
        subscriptionOutputCode = this.apiClient.subscription(customData);
    }

    private PotentialCustomerData convertToCustomData() {
        var customData = new PotentialCustomerData();
        customData.setCourseId(FIXTURE_COURSE.getId() + "");
        customData.setEmail(this.userData.get("EMAIL"));
        customData.setName(this.userData.get("FIRST NAME"));
        customData.setSurname(this.userData.get("SURNAME"));
        customData.setCompany(this.userData.get("COMPANY NAME"));
        customData.setDnicif(this.userData.get("NIF/CIF"));
        customData.setIsCompany(this.userData.get("IS COMPANY").equals("YES"));
        customData.setPhoneNumber(this.userData.get("PHONE NUMBER"));
        customData.setAddress(this.userData.get("ADDRESS"));
        customData.setPostalCode(this.userData.get("POSTAL CODE"));
        customData.setRegion(this.userData.get("REGION"));
        customData.setPayCometUserId(this.payCometUserId);
        customData.setTemporalPayCometToken(this.temporalPayCometToken);
        return customData;
    }

    @Then("the customer is informed about the success of the subscription")
    public void the_customer_is_informed_about_the_success_of_the_subscription() {
        assertThat(subscriptionOutputCode).isEqualTo(TestApiClient.SUCCESS_CODE);
    }

    @Then("the customer will receive an invoice to the recipients {string} with the following data")
    public void the_customer_will_receive_an_invoice_to_the_recipients_with_the_following_data(String emails, DataTable dataTable) {
        var invoiceDataList = dataTable.asMaps(String.class, String.class);
        assertThat(invoiceDataList).isNotEqualTo(1);
        var invoiceDataRow = invoiceDataList.get(0);
        List<HoldedCreationDataInvoice> sentInvoices = holdedApiClient.getSentInvoices(emails);
        assertThat(sentInvoices.size()).isEqualTo(1);
        var sentInvoice = sentInvoices.get(0);
        assertThat(sentInvoice.getItems().size()).isEqualTo(1);
        var item = sentInvoice.getItems().get(0);
        var concept = invoiceDataRow.get("CONCEPT");
        var units = Double.parseDouble(invoiceDataRow.get("UNITS"));
        var subtotal = Double.parseDouble(invoiceDataRow.get("SUBTOTAL"));

        assertThat(concept).isEqualTo(item.getName());
        assertThat(units).isEqualTo(item.getUnits());
        assertThat(subtotal).isEqualTo(item.getSubtotal());
    }

    @Then("the customer will receive access to the platform in the email {string} with the user {string}")
    public void the_customer_will_receive_access_to_the_platform_in_the_email_with_the_user(String string, String string2) {
    }
}
