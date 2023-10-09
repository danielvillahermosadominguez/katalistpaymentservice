package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.doubles.HoldedApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.MoodleApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.PayCometApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestApiClient;
import com.codurance.katalyst.payment.application.api.PotentialCustomerData;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedBillAddress;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedTypeContact;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.NotValidEMailFormat;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;


public class StepdefsSubscribeAndPaymentFeature {
    public static final int NO_ANSWER = -10;
    public static MoodleCourse FIXTURE_COURSE = null;

    private int subscriptionOutputCode = -1;
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
        var response = this.apiClient.checkItsAlive();

        if (!response.getBody().equals("OK! Working")) {
            fail();
        }

        moodleApiClient.reset();
        holdedApiClient.reset();
        subscriptionResult = NO_ANSWER;
    }

    @Given("Holded has no contacts")
    public void holded_has_no_contacts() {
        var contacts = holdedApiClient.getAllContacts();
        assertThat(contacts.size()).isEqualTo(0);
    }

    @Given("Holded which has these previous contacts")
    public void holded_which_has_these_previous_contacts(DataTable dtPreviousContacts) {
        var contactList = dtPreviousContacts.asMaps(String.class, String.class);
        var previousContactList = createContactList(contactList);
        for (var contact : previousContactList) {
            holdedApiClient.createContact(contact);
        }
        var currentContactList = holdedApiClient.getAllContacts();
        assertThat(contactList.size()).isEqualTo(currentContactList.size());
    }

    @Given("Moodle has not students")
    public void moodle_has_not_students() {
        var students = moodleApiClient.getAllUsers();
        assertThat(students.size()).isEqualTo(0);
    }

    @Given("Moodle which has these previous users")
    public void moodle_which_has_these_previous_users(DataTable dataTable) {
        var userList = dataTable.asMaps(String.class, String.class);
        var previousUserList = createUserList(userList);
        for (var user : previousUserList) {
            moodleApiClient.createUser(user);
        }
        var currentUsersList = moodleApiClient.getAllUsers();
        assertThat(userList.size()).isEqualTo(currentUsersList.size());
    }

    private List<MoodleUser> createUserList(List<Map<String, String>> userList) {
        return userList
                .stream()
                .map(data -> convertToMoodleUser(data))
                .collect(Collectors.toList());
    }

    private MoodleUser convertToMoodleUser(Map<String, String> data) {
        var name = data.get("NAME");
        var surname = data.get("SURNAME");
        var userName = data.get("USERNAME");
        var email = data.get("EMAIL");
        return new MoodleUser(name, surname, userName, email);
    }

    @Given("a previous course called {string} exists which has the following students")
    public void a_previous_course_exist_which_have_the_following_students(String courseName, DataTable dataTable) throws CustomFieldNotExists {
        var userList = dataTable.asMaps(String.class, String.class);
        var enrolledStudents = createUserList(userList);
        var course = moodleApiClient.addCourse(
                courseName,
                new MoodlePrice("0")
        );
        assertThat(course).isNotNull();
        assertThat(course.getDisplayname()).isEqualTo(courseName);
        for (var student : enrolledStudents) {
            moodleApiClient.enrolToTheCourse(course, student);
        }
    }

    @Given("An customer who has chosen the following course the course {string} with a price of {string}")
    public void an_customer_who_has_chosen_the_following_course_the_course_with_a_price_of(String courseName, String priceText) throws CustomFieldNotExists {
        var price = new MoodlePrice(priceText);
        assertThat(price).isNotEqualTo(0);
        var course = moodleApiClient.getCourseByName(courseName);
        course = (course == null)
                ? moodleApiClient.addCourse(courseName, price)
                : moodleApiClient.updatePrice(course.getId(), price);

        FIXTURE_COURSE = course;
        assertThat(FIXTURE_COURSE).isNotNull();
    }

    @Given("the customer has filled the following data")
    public void the_customer_has_filled_the_following_data(DataTable dtUserData) {
        assertThat(FIXTURE_COURSE).isNotNull();
        var rows = dtUserData.asMaps(String.class, String.class);
        assertThat(rows.size()).isEqualTo(1);
        userData = rows.get(0);
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
        var sentInvoices = holdedApiClient.getSentInvoices(emails);
        assertThat(sentInvoices.size()).isEqualTo(1);
        var sentInvoice = sentInvoices.get(0);
        var items = holdedApiClient.getSentItemsInTheResponseFor(sentInvoice);
        assertThat(items.size()).isEqualTo(1);
        var item = items.get(0);
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
        assertThat(paymentData.size()).isEqualTo(1);
        creditDebitCardData = paymentData.get(0);
        temporalPayCometToken = this.payCometApiClient.generateTemporalToken();
        var customData = convertToCustomData();
        subscriptionOutputCode = this.apiClient.subscription(customData);
    }

    @Then("the customer will receive access to the platform in the email {string} with the user {string} and fullname {string} {string}")
    public void the_customer_will_receive_access_to_the_platform_in_the_email_with_the_user_and_name(String moodleEmail, String moodleUser, String moodleName, String moodleSurname) {
        var user = moodleApiClient.getUserByMail(moodleEmail);
        assertThat(user).isNotNull();
        assertThat(user.getUserName()).isEqualTo(moodleUser);
        assertThat(user.getName()).isEqualTo(moodleName);
        assertThat(user.getLastName()).isEqualTo(moodleSurname);
    }


    @Then("Holded has the following contacts")
    public void holded_has_the_following_contacts(DataTable dtContacts) {
        var contactList = dtContacts.asMaps(String.class, String.class);
        var expectedContactList = createContactList(contactList);
        var currentContactList = holdedApiClient.getAllContacts();
        assertThat(currentContactList.size()).isEqualTo(expectedContactList.size());
        for (var contact : expectedContactList) {
            assertThat(existInTheList(contact, currentContactList)).isTrue();
        }
    }

    private boolean existInTheList(HoldedContact contact, List<HoldedContact> currentContactList) {
        for (var currentContact : currentContactList) {
            if (contact.haveSameMainData(currentContact)) {
                return true;
            }
        }
        return false;
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
        customData.setCountry(this.userData.get("COUNTRY"));
        customData.setUsername(this.creditDebitCardData.get("NAME"));
        customData.setPaytpvToken(this.temporalPayCometToken);
        return customData;
    }

    private HoldedContact convertToHoldedContact(Map<String,String> holdedContactData) throws NotValidEMailFormat {
        var customId = holdedContactData.get("CUSTOMER-ID");
        var name = holdedContactData.get("NAME");
        var code = holdedContactData.get("CONTACT NIF");
        var vatNumber = holdedContactData.get("VAT NUMBER");
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

        var isPerson = thisContactIs.equals("Person");
        if (isPerson) {
            vatNumber = null;
        } else {
            code = null;
        }
        var contact = new HoldedContact(
                name,
                code,
                vatNumber,
                HoldedTypeContact.CLIENT,
                isPerson,
                new HoldedEmail(email),
                phoneNumber,
                billAddress,
                purchaseAccount
        );
        contact.setCustomId(customId);
        return contact;
    }

    private List<HoldedContact> createContactList(List<Map<String, String>> paymentData) {
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
        return expectedContactList;
    }
}
