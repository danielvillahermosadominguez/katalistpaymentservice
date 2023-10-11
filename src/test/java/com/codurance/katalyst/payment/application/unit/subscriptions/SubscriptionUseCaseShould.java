package com.codurance.katalyst.payment.application.unit.subscriptions;

import com.codurance.katalyst.payment.application.api.CustomerData;
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.ports.PayCometApiClient;
import com.codurance.katalyst.payment.application.ports.holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedInvoiceInfo;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedTypeContact;
import com.codurance.katalyst.payment.application.ports.holded.exceptions.HoldedNotRespond;
import com.codurance.katalyst.payment.application.ports.holded.exceptions.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.usecases.SubscriptionUseCase;
import com.codurance.katalyst.payment.application.usecases.UserNameService;
import com.codurance.katalyst.payment.application.usecases.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.usecases.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.usecases.exception.HoldedIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.usecases.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.usecases.exception.UserIsEnroledInTheCourse;
import com.codurance.katalyst.payment.application.utils.DateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SubscriptionUseCaseShould {

    private HoldedApiClient holdedApiClient;
    private MoodleApiClient moodleApiClient;
    private PayCometApiClient payCometApiClient;
    private DateService dateService;
    private SubscriptionUseCase useCase;
    private UserNameService userNameService;
    private CustomerData customerData;
    private MoodleCourse course;
    private String courseId;

    @BeforeEach
    void beforeEach() throws CustomFieldNotExists, MoodleNotRespond {
        holdedApiClient = mock(HoldedApiClient.class);
        moodleApiClient = mock(MoodleApiClient.class);
        payCometApiClient = mock(PayCometApiClient.class);
        dateService = mock(DateService.class);
        userNameService = mock(UserNameService.class);
        useCase = new SubscriptionUseCase(
                holdedApiClient,
                moodleApiClient,
                payCometApiClient,
                dateService,
                userNameService
        );
        customerData = new CustomerData();
        customerData.setPaytpvToken("RANDOM_TPV_TOKEN");
        when(payCometApiClient.createUser(any())).thenReturn(new CreatedUser());
        when(payCometApiClient.payment(
                anyDouble(),
                any(),
                anyInt(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(new PaymentStatus());
        course = new MoodleCourse(1,
                "random_display_name",
                new MoodlePrice("14.5")
        );
        courseId = course.getId() + "";
        customerData.setCourseId(courseId);
        when(moodleApiClient.getCourse(course.getId() + "")).thenReturn(course);
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void throw_an_exception_if_customer_data_dont_have_tpv_token(String token) {
        customerData.setPaytpvToken(token);
        var thrown = assertThrows(TPVTokenIsRequired.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_paycomet_dont_return_an_user() {
        customerData.setCourseId("1");
        customerData.setPaytpvToken("RANDOM_TOKEN");
        when(payCometApiClient.createUser(any())).thenReturn(null);
        var thrown = assertThrows(CreditCardNotValid.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_moodle_not_respond() throws MoodleNotRespond {
        when(moodleApiClient.getCourse(any())).thenThrow(MoodleNotRespond.class);
        var thrown = assertThrows(LearningPlatformIsNotAvailable.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_the_course_exists_in_moodle() {
        customerData.setCourseId("5");
        var thrown = assertThrows(CourseNotExists.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_the_email_is_not_valid() {
        customerData.setEmail("RANDOM_NO_VALID_EMAIL");

        var thrown = assertThrows(InvalidInputCustomerData.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void check_if_the_course_exists_in_moodle() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NoPriceAvailable, CustomFieldNotExists, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid, NotValidEMailFormat {
        var email = new HoldedEmail("random_username@random_domain.com");
        var nifCif = "46842041C";
        var contact = createBasicContact(email, nifCif);

        customerData.setDnicif(nifCif);
        customerData.setEmail(email.getValue());
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(contact);

        useCase.subscribe(customerData);

        verify(moodleApiClient).getCourse(courseId);
    }

    @Test
    void throw_an_exception_if_the_customer_is_already_enrolled_in_the_course() throws HoldedNotRespond, MoodleNotRespond, CustomFieldNotExists, NotValidEMailFormat {
        var email = new HoldedEmail("random_username@random_domain.com");
        var nifCif = "46842041C";
        var contact = createBasicContact(email, nifCif);

        customerData.setCourseId(courseId);
        customerData.setDnicif(nifCif);
        customerData.setEmail(email.getValue());
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(contact);
        when(moodleApiClient.existsAnUserinThisCourse(any(), any())).thenReturn(true);

        var thrown = assertThrows(UserIsEnroledInTheCourse.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_holded_not_respond() throws HoldedNotRespond, NotValidEMailFormat {
        var nifCif = "46842041C";
        var email = new HoldedEmail("random_username@random_domain.com");
        customerData.setEmail(email.getValue());
        customerData.setDnicif(nifCif);
        when(holdedApiClient.getContactByCustomId(any())).thenThrow(HoldedNotRespond.class);

        var thrown = assertThrows(HoldedIsNotAvailable.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void not_to_create_a_contact_if_the_contact_exists_in_holded() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, NoPriceAvailable, CustomFieldNotExists, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var email = new HoldedEmail("random_username@random_domain.com");
        var nifCif = "46842041C";
        var contact = createBasicContact(email, nifCif);
        var expectedCustomId = contact.getCustomId();
        customerData.setEmail(email.getValue());
        customerData.setDnicif(nifCif);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(contact);

        useCase.subscribe(customerData);

        verify(holdedApiClient).getContactByCustomId(expectedCustomId);
        verify(holdedApiClient, never()).createContact(any());
    }

    @Test
    void create_a_contact_with_upper_case_data_when_contact_not_exists() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, NoPriceAvailable, CustomFieldNotExists, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var contactArg = ArgumentCaptor.forClass(HoldedContact.class);
        var companyName = "random company";
        var nifCif = "46842041c";
        var address = "random direction";
        var postalCode = "28080";
        var region = "random region";
        var city = "random city";
        var email = new HoldedEmail("random_username@random_domain.com");
        var contact = createBasicContact(email, nifCif.toUpperCase());
        var expectedCustomId = contact.getCustomId();
        fillCustomerData(course.getId() + "",
                email.getValue(),
                "John",
                "Doe",
                postalCode,
                address,
                "916185445",
                companyName,
                true,
                region,
                city,
                nifCif
        );

        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(holdedApiClient.createContact(any())).thenReturn(contact);

        useCase.subscribe(customerData);

        verify(holdedApiClient).getContactByCustomId(expectedCustomId);
        verify(holdedApiClient).createContact(contactArg.capture());
        contact = contactArg.getValue();
        assertThat(contact.getName()).isEqualTo(companyName.toUpperCase());
        assertThat(contact.getCode()).isNull();
        assertThat(contact.getVatNumber()).isEqualTo(nifCif.toUpperCase());
        assertThat(contact.getBillAddress()).isNotNull();
        var bilAddress = contact.getBillAddress();
        assertThat(bilAddress.getAddress()).isEqualTo(address.toUpperCase());
        assertThat(bilAddress.getPostalCode()).isEqualTo(postalCode.toUpperCase());
        assertThat(bilAddress.getCity()).isEqualTo(city.toUpperCase());
        assertThat(bilAddress.getProvince()).isEqualTo(region.toUpperCase());
    }

    @Test
    void create_an_invoice_with_the_course_data() throws CourseNotExists, HoldedNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var email = "randomusername@randomdomain.com";
        var nifCif = "46842041c";
        var contact = createBasicContact(
                new HoldedEmail(email),
                nifCif
        );
        var invoice = new HoldedInvoiceInfo();
        fillCustomerData(courseId,
                email,
                "John",
                "Doe",
                "28080",
                "random direction",
                "916185445",
                "random company",
                true,
                "random region",
                "random city",
                nifCif
        );
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(holdedApiClient.createContact(any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);

        useCase.subscribe(customerData);

        verify(holdedApiClient).createInvoice(
                any(),
                eq(course.getDisplayname()),
                eq(""),
                eq(1),
                eq(14.5)
        );
    }

    @Test
    void throw_an_exception_when_price_not_exist_in_moodle() throws HoldedNotRespond, MoodleNotRespond, NotValidEMailFormat, CustomFieldNotExists {
        var courseId = "100";
        var course = mock(MoodleCourse.class);
        when(course.getId()).thenReturn(Integer.parseInt(courseId));
        when(course.getDisplayname()).thenReturn("random_display_name");
        when(course.getPrice()).thenThrow(CustomFieldNotExists.class);
        var email = "random_username@random_domain.com";
        var nifCif = "46842041c";
        var contact = createBasicContact(
                new HoldedEmail(email),
                nifCif
        );
        var invoice = new HoldedInvoiceInfo();
        fillCustomerData(courseId,
                email,
                "John",
                "Doe",
                "28080",
                "random direction",
                "916185445",
                "random company",
                false,
                "random region",
                "random city",
                nifCif
        );
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse(courseId)).thenReturn(course);
        when(holdedApiClient.createContact(any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);

        var thrown = assertThrows(NoPriceAvailable.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void send_the_invoice_to_the_customer_email() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var email = "random_username@random_domain.com";
        var nifCif = "46842041c";
        var contact = createBasicContact(
                new HoldedEmail(email),
                nifCif
        );
        var invoice = new HoldedInvoiceInfo();
        fillCustomerData(courseId,
                email,
                "John",
                "Doe",
                "28080",
                "random direction",
                "916185445",
                "random company",
                true, // Esto esta mal
                "random region",
                "random city",
                nifCif
        );

        ArgumentCaptor<List<HoldedEmail>> captor = ArgumentCaptor.forClass(List.class);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(holdedApiClient.createContact(any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);
        when(holdedApiClient.sendInvoice(any(), captor.capture())).thenReturn(new HoldedStatus());

        useCase.subscribe(customerData);

        verify(holdedApiClient).sendInvoice(
                any(),
                any()
        );

        var emails = captor.getValue();
        assertThat(emails.size()).isEqualTo(1);
        assertThat(emails.get(0).getValue()).isEqualTo(email);
    }

    @Test
    void enrolle_a_new_user_in_moodle_when_is_person() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var username = "random_username_propose_for_service";
        when(userNameService.getAProposalForUserNameBasedOn(any())).thenReturn(username);
        var userId = "1";
        var email = "random_username@random_domain.com";
        var surname = "Doe";
        var nifCif = "46842041c";
        var student = new MoodleUser(
                userId,
                "John",
                surname,
                username,
                email
        );

        var contact = createBasicContact(
                new HoldedEmail(email),
                nifCif
        );

        var invoice = new HoldedInvoiceInfo();
        fillCustomerData(courseId,
                email,
                "John",
                surname,
                "28080",
                "random direction",
                "916185445",
                "random company",
                false,
                "random region",
                "random city",
                nifCif
        );

        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getUserByMail(email)).thenReturn(null);
        when(holdedApiClient.createContact(any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);
        when(moodleApiClient.createUser(any())).thenReturn(student);
        var captorCourse = ArgumentCaptor.forClass(MoodleCourse.class);
        var captorUser = ArgumentCaptor.forClass(MoodleUser.class);
        when(holdedApiClient.sendInvoice(any(), any())).thenReturn(new HoldedStatus());

        useCase.subscribe(customerData);

        verify(moodleApiClient).createUser(
                new MoodleUser(
                        customerData.getName(),
                        customerData.getSurname(),
                        username,
                        customerData.getEmail()
                )
        );

        verify(moodleApiClient).enrolToTheCourse(
                captorCourse.capture(),
                captorUser.capture()
        );

        var enrolledUser = captorUser.getValue();
        var enrolledCourse = captorCourse.getValue();
        assertThat(enrolledCourse.getId()).isEqualTo(course.getId());
        assertThat(enrolledUser.getId()).isEqualTo(userId);
        assertThat(enrolledUser.getName()).isEqualTo("John");
        assertThat(enrolledUser.getLastName()).isEqualTo(surname);
        assertThat(enrolledUser.getUserName()).isEqualTo(username);
        assertThat(enrolledUser.getEmail()).isEqualTo(email);
    }

    @Test
    void enrolle_a_new_user_in_moodle_when_is_company() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var username = "random_username_propose_for_service";
        when(userNameService.getAProposalForUserNameBasedOn(any())).thenReturn(username);
        var userId = "1";
        var email = "random_username@random_domain.com";
        var firstName = "N/A";
        var surname = "N/A";
        var companyName = "RANDOM_COMPANY NAME S.A";
        var student = new MoodleUser(
                userId,
                companyName,
                "",
                username,
                email
        );

        var nifCif = "ES468420";
        var contact = createBasicContact(
                new HoldedEmail(email),
                nifCif
        );

        var invoice = new HoldedInvoiceInfo();
        fillCustomerData(courseId,
                email,
                firstName,
                surname,
                "28080",
                "random direction",
                "916185445",
                companyName,
                true,
                "random region",
                "random city",
                nifCif
        );

        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getUserByMail(email)).thenReturn(null);
        when(holdedApiClient.createContact(any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);
        when(moodleApiClient.createUser(any())).thenReturn(student);
        var captorCourse = ArgumentCaptor.forClass(MoodleCourse.class);
        var captorUser = ArgumentCaptor.forClass(MoodleUser.class);
        when(holdedApiClient.sendInvoice(any(), any())).thenReturn(new HoldedStatus());

        useCase.subscribe(customerData);

        verify(moodleApiClient).createUser(
                new MoodleUser(
                        customerData.getCompany(),
                        "",
                        username,
                        customerData.getEmail()
                )
        );

        verify(moodleApiClient).enrolToTheCourse(
                captorCourse.capture(),
                captorUser.capture()
        );

        var enrolledUser = captorUser.getValue();
        var enrolledCourse = captorCourse.getValue();
        assertThat(enrolledCourse.getId()).isEqualTo(course.getId());
        assertThat(enrolledUser.getId()).isEqualTo(userId);
        assertThat(enrolledUser.getName()).isEqualTo(companyName);
        assertThat(enrolledUser.getUserName()).isEqualTo(username);
        assertThat(enrolledUser.getEmail()).isEqualTo(email);
    }

    @Test
    void enrolle_an_existent_user_in_moodle_when_is_person() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var userId = "1";
        var email = "random_username@random_domain.com";
        var firstName = "John";
        var surname = "Doe";
        var username = "random_username";
        var student = new MoodleUser(
                userId,
                firstName,
                surname,
                username,
                email
        );

        var nifCif = "46842041c";
        var contact = createBasicContact(
                new HoldedEmail(email),
                nifCif
        );

        var invoice = new HoldedInvoiceInfo();

        fillCustomerData(courseId,
                email,
                firstName,
                surname,
                "28080",
                "random direction",
                "916185445",
                "random company",
                true,
                "random region",
                "random city",
                nifCif
        );

        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getUserByMail(email)).thenReturn(student);
        when(holdedApiClient.createContact(any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);

        var captorCourse = ArgumentCaptor.forClass(MoodleCourse.class);
        var captorUser = ArgumentCaptor.forClass(MoodleUser.class);
        var status = mock(HoldedStatus.class);
        when(holdedApiClient.sendInvoice(any(), any())).thenReturn(status);

        useCase.subscribe(customerData);

        verify(moodleApiClient, never()).createUser(
                any()
        );

        verify(moodleApiClient).enrolToTheCourse(
                captorCourse.capture(),
                captorUser.capture()
        );

        var enrolledUser = captorUser.getValue();
        var enrolledCourse = captorCourse.getValue();
        assertThat(enrolledCourse.getId()).isEqualTo(course.getId());
        assertThat(enrolledUser.getId()).isEqualTo(userId);
        assertThat(enrolledUser.getEmail()).isEqualTo(email);
    }

    private HoldedContact createBasicContact(HoldedEmail email, String nifCif) {
        var contact = new HoldedContact(
                "RANDOM_NAME",
                nifCif,
                "46842041C", HoldedTypeContact.CLIENT,
                true,
                email,
                "RANDOM_PHONE",
                null,
                "RANDOM_PURCHASE_ACCOUNT"
        );
        return contact;
    }

    private void fillCustomerData(String course,
                                  String email,
                                  String firstName,
                                  String surname,
                                  String postalCode,
                                  String direction,
                                  String phone,
                                  String company,
                                  boolean isCompany,
                                  String region,
                                  String city,
                                  String nifCif) {
        customerData.setCourseId(course);
        customerData.setEmail(email);
        customerData.setName(firstName);
        customerData.setSurname(surname);
        customerData.setPostalCode(postalCode);
        customerData.setAddress(direction);
        customerData.setPhoneNumber(phone);
        customerData.setCompany(company);
        customerData.setIsCompany(isCompany);
        customerData.setRegion(region);
        customerData.setCity(city);
        customerData.setDnicif(nifCif);
    }
}
