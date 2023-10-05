package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.doubles.HoldedApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.MoodleApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.PayCometApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestApiClient;
import com.codurance.katalyst.payment.application.api.PotentialCustomerData;
import com.codurance.katalyst.payment.application.holded.dto.HoldedBillAddress;
import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.holded.dto.HoldedTypeContact;
import com.codurance.katalyst.payment.application.holded.dto.NotValidEMailFormat;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;


public class StepdefsSubscribeAndPaymentFeature {
    public static final int NO_ANSWER = -10;
    public static MoodleCourse FIXTURE_COURSE = null;

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
    @Autowired
    private PayCometApiClientFake payCometApiClient;

    private int subscriptionResult = NO_ANSWER;
    private Map<String, String> userData = null;
    private Map<String, String> creditDebitCardData = null;
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

    @Given("Holded has no contacts")
    public void holded_has_no_contacts() {
        var contacts = holdedApiClient.getAllContacts();
        assertThat(contacts.size()).isEqualTo(0);
    }
    @Given("Moodle has not students")
    public void moodle_has_not_students() {
        var students = moodleApiClient.getAllUsers();
        assertThat(students.size()).isEqualTo(0);
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

    @When("the customer pays the subscription with credit\\/debit card with the following result")
    public void the_customer_pays_the_subscription_with_credit_debit_card_with_the_following_result(DataTable dataTable) {
        var paymentData = dataTable.asMaps(String.class, String.class);
        assertThat(paymentData).isNotEqualTo(1);
        this.creditDebitCardData = paymentData.get(0);
        this.temporalPayCometToken = this.payCometApiClient.generateTemporalToken();
        var customData = convertToCustomData();
        subscriptionOutputCode = this.apiClient.subscription(customData);
    }

    @Then("the customer will receive access to the platform in the email {string} with the user {string}")
    public void the_customer_will_receive_access_to_the_platform_in_the_email_with_the_user(String string, String string2) {
    }

    @Then("Holded has the following contacts")
    public void holded_has_the_following_contacts(DataTable dataTable) {
        var paymentData = dataTable.asMaps(String.class, String.class);
        var expectedContactList = paymentData
                .stream()
                .map(data -> {
                    try {
                        return convertToHoldedContact(data);
                    } catch (NotValidEMailFormat e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        List<HoldedContact> currentContactList = this.holdedApiClient.getAllContacts();
        assertThat(currentContactList.size()).isEqualTo(expectedContactList.size());
        for (var contact: expectedContactList) {
            assertThat(contact).isIn(currentContactList);
        }

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
        customData.setCity(this.userData.get("CITY"));
        customData.setRegion(this.userData.get("REGION"));
        customData.setUsername(this.creditDebitCardData.get("NAME"));
        customData.setPaytpvToken(this.temporalPayCometToken);
        return customData;
    }

    private HoldedContact convertToHoldedContact(Map<String,String> holdedContactData) throws NotValidEMailFormat {
        var customId = holdedContactData.get("CUSTOMER-ID");
        var name = holdedContactData.get("NAME");
        var code = holdedContactData.get("CONTACT NIF");
        var thisContactIs = holdedContactData.get("THIS CONTACT IS");
        var email = holdedContactData.get("EMAIL");
        var address = holdedContactData.get("ADDRESS");
        var phoneNumber = holdedContactData.get("PHONE NUMBER");
        var postalCode = holdedContactData.get("POSTAL CODE");
        var province = holdedContactData.get("PROVINCE");
        var city = holdedContactData.get("CITY");
        var country = holdedContactData.get("COUNTRY");
        var purchaseAccount = holdedContactData.get("PURCHASE ACCOUNT");
        var billAddress = new HoldedBillAddress(address, postalCode, city, province, country);

        var contact = new HoldedContact(
                name,
                code,
                HoldedTypeContact.valueOf(thisContactIs),
                new HoldedEmail(email),
                phoneNumber,
                billAddress,
                purchaseAccount
        );
        contact.setCustomId(customId);
        return contact;
    }
}
