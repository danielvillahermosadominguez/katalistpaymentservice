package com.codurance.katalyst.payment.application.unit.actions;

import com.codurance.katalyst.payment.application.actions.SubscribeToCourse;
import com.codurance.katalyst.payment.application.actions.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.actions.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.actions.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.actions.exception.UserIsEnroledInTheCourse;
import com.codurance.katalyst.payment.application.model.customer.CustomerData;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.learning.entity.Course;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubscribeToCourseShould {
    public static final String RANDOM_IP = "RANDOM_IP";
    public static final String RANDOM_TPV_TOKEN = "RANDOM_TPV_TOKEN";
    public static final String RANDOM_COURSE_NAME = "RANDOM_COURSE_NAME";
    public static final double PRICE = 13.5;
    private PaymentService paymentService;
    private LearningService learningService;
    private SubscribeToCourse subscribeToCourse;

    private PurchaseService purchaseService;

    private CustomerData customerData;
    private PaymentTransaction expectedPaymentTransaction;

    @BeforeEach
    void beforeEach() {
        paymentService = mock(PaymentService.class);
        learningService = mock(LearningService.class);
        purchaseService = mock(PurchaseService.class);
        customerData = createCustomerDataFixture();
        subscribeToCourse = new SubscribeToCourse(
                paymentService,
                learningService,
                purchaseService);
        expectedPaymentTransaction = createPaymentTransactionFixture();
    }

    @Test
    void be_sure_the_course_is_available() throws CourseNotExists, NoPriceAvailable, LearningPlatformIsNotAvailable, FinancialPlatformIsNotAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid, CustomFieldNotExists, MoodleNotRespond {
        var courseId = "1";
        customerData.setCourseId(courseId);
        prepareStubsForNormalFlow(courseId);

        subscribeToCourse.subscribe(customerData);

        verify(learningService, times(1))
                .getCourse(courseId);
    }

    @Test
    void throw_an_exception_if_course_doesnt_exist() {
        var courseId = "1";
        customerData.setCourseId(courseId);

        var exception = Assert.assertThrows(CourseNotExists.class, () -> {
            subscribeToCourse.subscribe(customerData);
        });

        assertThat(exception).isNotNull();
    }

    @Test
    void check_availability_for_this_customer() throws TPVTokenIsRequired, CourseNotExists, NoPriceAvailable, LearningPlatformIsNotAvailable, FinancialPlatformIsNotAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, CreditCardNotValid, CustomFieldNotExists, MoodleNotRespond {
        var courseId = "1";
        customerData.setCourseId(courseId);
        prepareStubsForNormalFlow(courseId);

        subscribeToCourse.subscribe(customerData);

        verify(learningService, times(1))
                .isThereASeatFor(
                        courseId,
                        customerData.getEmail());
    }

    @Test
    void authorize_the_payment_transaction() throws TPVTokenIsRequired, CourseNotExists, NoPriceAvailable, LearningPlatformIsNotAvailable, FinancialPlatformIsNotAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, CreditCardNotValid, CustomFieldNotExists, MoodleNotRespond {
        var courseId = "1";
        customerData.setCourseId(courseId);
        expectedPaymentTransaction.setPaymentStatus(new PaymentStatus());
        prepareStubsForNormalFlow(courseId);

        var paymentStatus = subscribeToCourse.subscribe(customerData);

        verify(paymentService, times(1))
                .authorizeTransaction(RANDOM_IP, RANDOM_TPV_TOKEN, PRICE);
        assertThat(paymentStatus).isNotNull();
    }

    @Test
    void save_customer_data_when_payment_status_is_not_null() throws TPVTokenIsRequired, CourseNotExists, NoPriceAvailable, LearningPlatformIsNotAvailable, FinancialPlatformIsNotAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, CreditCardNotValid, CustomFieldNotExists, MoodleNotRespond {
        var courseId = "1";
        var purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        customerData.setCourseId(courseId);
        var expectedPurchase = createPurchaseFromCustomData(courseId);

        when(purchaseService.save(any())).thenReturn(expectedPurchase);
        expectedPaymentTransaction.setPaymentStatus(new PaymentStatus());
        prepareStubsForNormalFlow(courseId);

        var paymentStatus = subscribeToCourse.subscribe(customerData);

        verify(purchaseService, times(1))
                .save(purchaseCaptor.capture());
        assertThat(paymentStatus).isNotNull();
        var purchase = purchaseCaptor.getValue();
        assertThat(purchase).isNotNull();
        assertThat(purchase.getOrder()).isEqualTo(expectedPaymentTransaction.getOrder());
        assertThatPurchaseDataIsEqualToCustomerData(purchase);
        assertThat(purchase.getConcept()).isEqualTo(RANDOM_COURSE_NAME);
        assertThat(purchase.getDescription()).isEqualTo("");
        assertThat(purchase.getPrice()).isEqualTo(PRICE);
        assertThat(purchase.isProcessedInFinantialState()).isEqualTo(false);
        assertThat(purchase.isProcessedInLearningState()).isEqualTo(false);
    }

    private void assertThatPurchaseDataIsEqualToCustomerData(Purchase purchase) {
        assertThat(purchase.getCourseId()).isEqualTo(customerData.getCourseId());
        assertThat(purchase.getEmail()).isEqualTo(customerData.getEmail());
        assertThat(purchase.getName()).isEqualTo(customerData.getName());
        assertThat(purchase.getSurname()).isEqualTo(customerData.getSurname());
        assertThat(purchase.getNifCif()).isEqualTo(customerData.getDnicif());
        assertThat(purchase.isCompany()).isEqualTo(customerData.getIsCompany());
        assertThat(purchase.getCity()).isEqualTo(customerData.getCity());
        assertThat(purchase.getRegion()).isEqualTo(customerData.getRegion());
        assertThat(purchase.getCountry()).isEqualTo(customerData.getCountry());
    }

    private Purchase createPurchaseFromCustomData(String courseId) {
        return new Purchase(
                expectedPaymentTransaction.getId(),
                expectedPaymentTransaction.getOrder(),
                courseId,
                RANDOM_COURSE_NAME,
                "",
                PRICE,
                customerData.getEmail(),
                customerData.getName(),
                customerData.getSurname(),
                customerData.getDnicif(),
                customerData.getIsCompany(),
                customerData.getCompany(),
                customerData.getPhoneNumber(),
                customerData.getAddress(),
                customerData.getPostalCode(),
                customerData.getCity(),
                customerData.getRegion(),
                customerData.getCountry(),
                false,
                false
        );
    }

    @Test
    void not_save_customer_data_when_payment_status_is_null() throws TPVTokenIsRequired, CourseNotExists, NoPriceAvailable, LearningPlatformIsNotAvailable, FinancialPlatformIsNotAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, CreditCardNotValid, CustomFieldNotExists, MoodleNotRespond {
        var courseId = "1";
        customerData.setCourseId(courseId);
        expectedPaymentTransaction.setPaymentStatus(null);
        prepareStubsForNormalFlow(courseId);

        var paymentStatus = subscribeToCourse.subscribe(customerData);

        verify(purchaseService, never())
                .save(any());
        assertThat(paymentStatus).isNull();
    }

    private void prepareStubsForNormalFlow(String courseId) throws TPVTokenIsRequired, CreditCardNotValid, NoPriceAvailable, LearningPlatformIsNotAvailable {
        when(learningService.isThereASeatFor(anyString(), anyString()))
                .thenReturn(true);
        when(learningService.getCourse(any())).thenReturn(
                new Course(Integer.parseInt(courseId), RANDOM_COURSE_NAME, PRICE)
        );
        when(paymentService.authorizeTransaction(RANDOM_IP, RANDOM_TPV_TOKEN, PRICE))
                .thenReturn(expectedPaymentTransaction);
    }

    private CustomerData createCustomerDataFixture() {
        var customerData = new CustomerData();
        customerData.setPaytpvToken(RANDOM_TPV_TOKEN);
        customerData.setIp(RANDOM_IP);
        return customerData;
    }

    private PaymentTransaction createPaymentTransactionFixture() {
        return new PaymentTransaction(
                1234,
                RANDOM_IP,
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION,
                RANDOM_TPV_TOKEN,
                1,
                "RANDOM_ORDER",
                34.56,
                "20231205103259",
                PaymentTransactionState.PENDING,
                null
        );
    }
}
