package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.doubles.HoldedApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.MoodleApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.PayCometApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestApiClient;
import com.codurance.katalyst.payment.application.actions.RetryPendingPayments;
import com.codurance.katalyst.payment.application.apirest.dto.Error;
import com.codurance.katalyst.payment.application.builders.CustomerDataBuilder;
import com.codurance.katalyst.payment.application.builders.HoldedContactBuilder;
import com.codurance.katalyst.payment.application.builders.MoodleUserBuilder;
import com.codurance.katalyst.payment.application.builders.PaymentTransactionBuilder;
import com.codurance.katalyst.payment.application.builders.PurchaseBuilder;
import com.codurance.katalyst.payment.application.infrastructure.database.payment.DBPaymentTransaction;
import com.codurance.katalyst.payment.application.infrastructure.database.payment.DBPaymentTransactionRepository;
import com.codurance.katalyst.payment.application.infrastructure.database.purchase.DBPurchase;
import com.codurance.katalyst.payment.application.infrastructure.database.purchase.DBPurchaseRepository;
import com.codurance.katalyst.payment.application.model.customer.CustomerData;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.ports.email.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentOrder;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;


public class StepdefsSubscribeAndPaymentFeature {
    public static final int NO_ANSWER = -10;
    public static final int WAIT_FOR_RETRY_TIMEOUT_IN_SECONDS = 2;
    public static final String OK = "OK";
    public static final String KO = "KO";
    public static final String ANY_IP = "127.0.0.1";
    public static final String HEALTHCHECK_OK_EXPECTED_RESPONSE = "OK! Working";
    public static MoodleCourse FIXTURE_COURSE = null;
    private int subscriptionOutputCode = -1;
    @LocalServerPort
    int randomServerPort;
    @Autowired
    private TestApiClient apiClient;
    @Autowired
    private MoodleApiClientFake moodleApiClient;
    @Autowired
    private HoldedApiClientFake holdedApiClient;
    @Autowired
    private PayCometApiClientFake payCometApiClient;
    @Autowired
    private DBPaymentTransactionRepository dbPaymentTransactionRepository;
    @Autowired
    private DBPurchaseRepository dbPurchaseRepository;
    @Autowired
    private RetryPendingPayments retryPendingPayments;
    @Value("${paycomet.terminal}")
    int tpvId;
    private int subscriptionResult = NO_ANSWER;
    private Map<String, String> userData = null;
    private Map<String, String> creditDebitCardData = null;
    private String temporalPayCometToken = null;
    private PaymentStatus paymentStatus;
    @Before
    public void beforeEachScenario() {
        if (!apiClient.isInitialized()) {
            apiClient.setPort(randomServerPort);
        }
        var response = apiClient.checkItsAlive();

        if (!response.getBody().equals(HEALTHCHECK_OK_EXPECTED_RESPONSE)) {
            fail();
        }

        moodleApiClient.reset();
        holdedApiClient.reset();
        payCometApiClient.reset();
        dbPurchaseRepository.deleteAll();
        dbPaymentTransactionRepository.deleteAll();
        retryPendingPayments.setActive(false);
        subscriptionResult = NO_ANSWER;
    }
    @After
    public void afterEach() {
        moodleApiClient.reset();
        holdedApiClient.reset();
        payCometApiClient.reset();
        dbPurchaseRepository.deleteAll();
        dbPaymentTransactionRepository.deleteAll();
        retryPendingPayments.setActive(false);
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
    public void moodle_which_has_these_previous_users(DataTable dataTable) throws MoodleNotRespond {
        var userList = dataTable.asMaps(String.class, String.class);
        var previousUserList = createUserList(userList);
        for (var user : previousUserList) {
            moodleApiClient.createUser(user);
        }
        var currentUsersList = moodleApiClient.getAllUsers();
        assertThat(userList.size()).isEqualTo(currentUsersList.size());
    }

    @Given("a previous course called {string} exists which has the following students")
    public void a_previous_course_exist_which_have_the_following_students(String courseName, DataTable dataTable) throws CustomFieldNotExists, MoodleNotRespond {
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
    @Given("the customer made a purchase with the following data")
    public void the_customer_made_a_purchase_with_the_following_data(DataTable purchaseTable) {
        var purchaseRows = purchaseTable.asMaps(String.class, String.class);
        assertThat(purchaseRows.size()).isEqualTo(1);
        var paymentTransaction = createPaymentTransaction();
        paymentTransaction.setTransactionState(PaymentTransactionState.PENDING);
        var dbPaymentTransaction = new DBPaymentTransaction(paymentTransaction);
        dbPaymentTransaction = dbPaymentTransactionRepository.save(dbPaymentTransaction);
        var transactionId = dbPaymentTransaction.getId();
        var purchase = convertToPurchase(purchaseRows.get(0), transactionId);
        var dbPurchase = new DBPurchase(purchase);
        dbPurchaseRepository.save(dbPurchase);
    }
    @Given("during the payment notification process, the learning platform didn't respond, but now is available")
    public void during_the_payment_notification_process_the_learning_platform_didn_t_respond_but_now_is_available() {
        setRetryStateAllPaymentTransactions();
        var dbPurchases = dbPurchaseRepository.findAll();
        for (var dbPurchase : dbPurchases) {
            dbPurchase.setLearningStepOvercome(false);
        }
        dbPurchaseRepository.saveAll(dbPurchases);
    }
    @Given("the retry process is active")
    public void the_retry_process_is_active() {
        retryPendingPayments.setActive(true);
    }
    @Given("during the payment notification process, the financial platform didn't respond, but now is available")
    public void during_the_payment_notification_process_the_financial_platform_didn_t_respond_but_now_is_available() {
        setRetryStateAllPaymentTransactions();
        var dbPurchases = dbPurchaseRepository.findAll();
        for (var dbPurchase : dbPurchases) {
            dbPurchase.setFinantialStepOvercome(false);
        }
        dbPurchaseRepository.saveAll(dbPurchases);
    }
    @Then("the customer is informed about the success of the subscription")
    public void the_customer_is_informed_about_the_success_of_the_subscription() {
        assertThat(paymentStatus).isNotNull();
        assertThat(paymentStatus.getChallengeUrl()).isNotEqualTo("");
        assertThat(apiClient.getLastErrors().size()).isEqualTo(0);
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

    @When("the customer pays the subscription with credit\\/debit card with the following data")
    public void the_customer_pays_the_subscription_with_credit_debit_card_with_the_following_data(DataTable dataTable) throws JsonProcessingException {
        var paymentData = dataTable.asMaps(String.class, String.class);
        assertThat(FIXTURE_COURSE).isNotNull();
        assertThat(this.userData).isNotNull();
        assertThat(paymentData.size()).isEqualTo(1);
        creditDebitCardData = paymentData.get(0);
        temporalPayCometToken = this.payCometApiClient.generateTemporalToken();
        var customData = convertToCustomData();
        paymentStatus = this.apiClient.paySubscription(customData);
    }

    @When("the customer receives a challenge URL and decide to {string} the payment")
    public void the_customer_receives_a_challenge_url_and_decide_to_the_payment(String decision) {
        var orders = payCometApiClient.getLastPaymentOrders();
        assertThat(orders.size()).isEqualTo(1);
        var order = orders.get(0);
        if (decision.equals("Accept")) {
            assertThat(paymentStatus).isNotNull();
            assertThat(apiClient.getLastErrors().size()).isNotNull();
            assertThat(paymentStatus.getChallengeUrl()).isEqualTo(PayCometApiClientFake.URL_CHALLENGE_OK);
            var notification = createAcceptNotification(order);
            assertThat(apiClient.confirmPayment(notification)).isTrue();
            return;
        }
        if (decision.equals("Cancel")) {
            assertThat(paymentStatus).isNotNull();
            assertThat(apiClient.getLastErrors().size()).isNotNull();
            assertThat(paymentStatus.getChallengeUrl()).isEqualTo(PayCometApiClientFake.URL_CHALLENGE_OK);
            var notification = createCancelNotification(order);
            assertThat(apiClient.confirmPayment(notification)).isTrue();
            return;
        }
        fail();
    }

    @Then("the customer will receive access to the platform in the email {string} with the user {string} and fullname {string} {string}")
    public void the_customer_will_receive_access_to_the_platform_in_the_email_with_the_user_and_name(String moodleEmail, String moodleUser, String moodleName, String moodleSurname) throws MoodleNotRespond {
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
        assertThat(holdedHasTheFollowingContacts(expectedContactList)).isTrue();
    }

    @Then("the customer is informed about the fail of the subscription")
    public void the_customer_is_informed_about_the_fail_of_the_subscription(DataTable dataTable) {
        var errorDescriptionData = dataTable.asMaps(String.class, String.class);
        var lastErrors = apiClient.getLastErrors();
        assertThat(errorDescriptionData.size()).isEqualTo(1);
        assertThat(lastErrors.size()).isEqualTo(1);
        var lastError = lastErrors.get(0);
        var expectedError = createError(errorDescriptionData.get(0));
        assertThat(lastError.getCode()).isEqualTo(expectedError.getCode());
        assertThat(lastError.getMessage()).isEqualTo(expectedError.getMessage());
    }

    @Then("There are not pending authorized payments")
    public void there_are_not_pending_authorized_payments() {
        var paymentTransactions = dbPaymentTransactionRepository.findAll();
        var existPendingTransactions = existPendingTransactions(paymentTransactions);
        var lastPaymentOrders = payCometApiClient.getLastPaymentOrders();
        assertThat(existPendingTransactions).isFalse();
        assertThat(lastPaymentOrders.size()).isEqualTo(0);
    }

    @Then("Moodle has the following users")
    public void moodle_has_the_following_users(DataTable dataTable) {
        var userList = dataTable.asMaps(String.class, String.class);
        var expectedUserList = createUserList(userList);
        assertThat(moodleHasTheFolowingUsers(expectedUserList)).isTrue();
    }

    @Then("the retry process finishes the notification process with the following contacts in holded")
    public void the_retry_process_finishes_the_notification_process_with_the_following_contacts_in_holded(DataTable dtContacts) {
        var contactList = dtContacts.asMaps(String.class, String.class);
        var expectedContactList = createContactList(contactList);
        await()
                .timeout(Duration.ofSeconds(WAIT_FOR_RETRY_TIMEOUT_IN_SECONDS))
                .untilAsserted(() -> assertThat(holdedHasTheFollowingContacts(expectedContactList)).isTrue());

    }

    @Then("the retry process finishes the notification process with the following users in Moodle")
    public void the_retry_process_finishes_the_notification_process_with_the_following_users_in_moodle(DataTable dataTable) {
        var userList = dataTable.asMaps(String.class, String.class);
        var expectedUserList = createUserList(userList);
        await()
                .timeout(Duration.ofSeconds(WAIT_FOR_RETRY_TIMEOUT_IN_SECONDS))
                .untilAsserted(() -> assertThat(moodleHasTheFolowingUsers(expectedUserList)).isTrue());
    }

    @Then("the customer is informed about the cancellation of the subscription")
    public void the_customer_is_informed_about_the_cancellation_of_the_subscription() {
        assertThat(paymentStatus).isNotNull();
        assertThat(paymentStatus.getChallengeUrl()).isNotEqualTo("");
        assertThat(apiClient.getLastErrors().size()).isEqualTo(0);
    }

    @Then("there are not contact in Holded")
    public void there_are_not_contact_in_holded() {
        var contacts = holdedApiClient.getAllContacts();
        assertThat(contacts.size()).isEqualTo(0);
    }

    @Then("there are not users in Moodle")
    public void there_are_not_users_in_moodle() {
        var users = moodleApiClient.getAllUsers();
        assertThat(users.size()).isEqualTo(0);
    }

    @Then("the customer doesn't receive any invoice to {string}")
    public void the_customer_doesn_t_receive_any_invoice_to(String email) {
        var holdedInvoiceInfos = holdedApiClient.getSentInvoices(email);
        assertThat(holdedInvoiceInfos.size()).isEqualTo(0);
    }

    @Then("the customer doesn't receive access to the platform in the email {string}")
    public void the_customer_doesn_t_receive_access_to_the_platform_in_the_email(String moodleEmail) throws MoodleNotRespond {
        var user = moodleApiClient.getUserByMail(moodleEmail);
        assertThat(user).isNull();
    }

    private boolean existInTheList(HoldedContact contact, List<HoldedContact> currentContactList) {
        for (var currentContact : currentContactList) {
            if (contact.haveSameMainData(currentContact)) {
                return true;
            }
        }
        return false;
    }

    private boolean existInTheList(MoodleUser user, List<MoodleUser> currentMoodleContactList) {
        for (var currentUser : currentMoodleContactList) {
            if (user.haveSameMainData(currentUser)) {
                return true;
            }
        }
        return false;
    }

    private CustomerData convertToCustomData() {
        var customerDataBuilder = new CustomerDataBuilder();
        return customerDataBuilder
                .createFromMap(userData)
                .courseId(FIXTURE_COURSE.getId() + "")
                .userName(this.creditDebitCardData.get("NAME"))
                .ip(ANY_IP)
                .payTpvToken(this.temporalPayCometToken)
                .getItem();
    }

    private HoldedContact convertToHoldedContact(Map<String, String> data) throws NotValidEMailFormat {
        var holdedContactBuilder = new HoldedContactBuilder();
        return holdedContactBuilder
                .createFromMap(data)
                .getItem();
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

    private List<MoodleUser> createUserList(List<Map<String, String>> userList) {
        return userList
                .stream()
                .map(data -> convertToMoodleUser(data))
                .collect(Collectors.toList());
    }

    private MoodleUser convertToMoodleUser(Map<String, String> data) {
        var moodleUserBuilder = new MoodleUserBuilder();
        return moodleUserBuilder
                .createFromMap(data)
                .getItem();
    }

    private Error createError(Map<String, String> errorDescriptionData) {
        var errorCode  = errorDescriptionData.get("ERROR CODE");
        var errorMessage  = errorDescriptionData.get("ERROR MESSAGE");

        return new Error(Integer.parseInt(errorCode), errorMessage);
    }

    private boolean existPendingTransactions(Iterable<DBPaymentTransaction> paymentTransactions) {
        boolean existPendingTransactions = false;
        for (DBPaymentTransaction paymentTransaction : paymentTransactions) {
            if (paymentTransaction.getTransactionState().equals(PaymentTransactionState.PENDING.getValue())) {
                existPendingTransactions = true;
                break;
            }
        }
        return existPendingTransactions;
    }

    private PaymentTransaction createPaymentTransaction() {
        var builder = new PaymentTransactionBuilder();
        return builder
                .createWithDefaultValues()
                .getItem();
    }

    private Purchase convertToPurchase(Map<String, String> purchaseMap, int transactionId) {
        var builder = new PurchaseBuilder();
        return builder
                .create(purchaseMap)
                .transactionId(transactionId)
                .financialStepOvercome(true)
                .learningStepOvercome(true)
                .getItem();
    }

    private boolean holdedHasTheFollowingContacts(List<HoldedContact> expectedContactList) {
        var currentContactList = holdedApiClient.getAllContacts();
        if (currentContactList.size() != expectedContactList.size()) {
            return false;
        }
        for (var contact : expectedContactList) {
            if (!existInTheList(contact, currentContactList)) {
                return false;
            }
        }
        return true;
    }

    private boolean moodleHasTheFolowingUsers(List<MoodleUser> expectedUserList) {
        var currentUsersList = moodleApiClient.getAllUsers();
        if (expectedUserList.size() != currentUsersList.size()) {
            return false;
        }
        for (var user : expectedUserList) {
            if (!existInTheList(user, currentUsersList)) {
                return false;
            }
        }
        return true;
    }

    private void setRetryStateAllPaymentTransactions() {
        var dbPaymentTransactions = dbPaymentTransactionRepository.findAll();
        for (var dbPaymentTransaction : dbPaymentTransactions) {
            dbPaymentTransaction.setTransactionState(PaymentTransactionState.RETRY.getValue());
        }
        dbPaymentTransactionRepository.saveAll(dbPaymentTransactions);
    }

    private PaymentNotification createCancelNotification(PaymentOrder order) {
        var amount = String.valueOf(order.getAmount());
        var notification = new PaymentNotification(
                PaymentMethod.fromInt(order.getMethodId()),
                TransactionType.AUTHORIZATION,
                tpvId,
                order.getOrder(),
                amount,
                KO
        );
        return notification;
    }

    private PaymentNotification createAcceptNotification(PaymentOrder order) {
        var amount = String.valueOf(order.getAmount());
        var notification = new PaymentNotification(
                PaymentMethod.fromInt(order.getMethodId()),
                TransactionType.AUTHORIZATION,
                tpvId,
                order.getOrder(),
                amount,
                OK
        );
        return notification;
    }
}
