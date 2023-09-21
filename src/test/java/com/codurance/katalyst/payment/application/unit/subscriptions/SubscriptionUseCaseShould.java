package com.codurance.katalyst.payment.application.unit.subscriptions;

import com.codurance.katalyst.payment.application.api.PotentialCustomerData;
import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.holded.dto.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.holded.exception.HoldedNotRespond;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.ports.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;
import com.codurance.katalyst.payment.application.ports.PayCometApiClient;
import com.codurance.katalyst.payment.application.usecases.SubscriptionUseCase;
import com.codurance.katalyst.payment.application.usecases.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.usecases.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.usecases.exception.HoldedIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.usecases.exception.MoodleIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.usecases.exception.UserIsEnroledInTheCourse;
import com.codurance.katalyst.payment.application.utils.DateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    private PotentialCustomerData customerData;

    @BeforeEach
    void beforeEach() {
        holdedApiClient = mock(HoldedApiClient.class);
        moodleApiClient = mock(MoodleApiClient.class);
        payCometApiClient = mock(PayCometApiClient.class);
        dateService = mock(DateService.class);
        useCase = new SubscriptionUseCase(holdedApiClient, moodleApiClient, payCometApiClient, dateService);
        customerData = new PotentialCustomerData();
        customerData.setPaytpvToken("RANDOM_TPV_TOKEN");
        //TEMPORAL
        when(payCometApiClient.createUser(any())).thenReturn(new CreatedUser());
        when(payCometApiClient.payment(anyDouble(), any(), anyInt(),any(),any(),any(),any())).thenReturn(new PaymentStatus());
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
         "",
            " "
    })
    void throw_an_exception_if_customer_data_dont_have_tpv_token(String token) {
        customerData.setCourseId("1");
        customerData.setPaytpvToken(token);
        var thrown = Assertions.assertThrows(TPVTokenIsRequired.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_paycomet_dont_return_an_user() {
        customerData.setCourseId("1");
        customerData.setPaytpvToken("RANDOM_TOKEN");
        when(payCometApiClient.createUser(any())).thenReturn(null);
        var thrown = Assertions.assertThrows(CreditCardNotValid.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_moodle_not_respond() throws MoodleNotRespond {
        customerData.setCourseId("1");
        when(moodleApiClient.getCourse(any())).thenThrow(MoodleNotRespond.class);
        var thrown = Assertions.assertThrows(MoodleIsNotAvailable.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_the_course_exists_in_moodle() throws MoodleNotRespond {
        customerData.setCourseId("1");

        var thrown = Assertions.assertThrows(CourseNotExists.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_the_email_is_not_valid() throws MoodleNotRespond, CustomFieldNotExists {
        customerData.setCourseId("1");
        customerData.setEmail("RANDOM_NO_VALID_EMAIL");
        var moodleCourse = mock(MoodleCourse.class);
        when(moodleCourse.getDisplayname()).thenReturn("RANDOM DISPLAY NAME");
        when(moodleCourse.getId()).thenReturn(1);
        when(moodleCourse.getPrice()).thenReturn(new MoodlePrice("100"));
        when(moodleApiClient.getCourse("1")).thenReturn(moodleCourse);

        var thrown = Assertions.assertThrows(InvalidInputCustomerData.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void check_if_the_course_exists_in_moodle() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NoPriceAvailable, CustomFieldNotExists, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var contact = mock(HoldedContact.class);
        when(contact.getEmail()).thenReturn("random_username@random_domain.com");
        var course = mock(MoodleCourse.class);
        when(course.getDisplayname()).thenReturn("random name");
        when(course.getPrice()).thenReturn(new MoodlePrice("33.4"));
        customerData.setCourseId("1");
        customerData.setDnicif("46842041C");
        customerData.setEmail("random_username@random_domain.com");
        when(moodleApiClient.getCourse("1")).thenReturn(course);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(contact);

        useCase.subscribe(customerData);

        verify(moodleApiClient).getCourse("1");
    }

    @Test
    void throw_an_exception_if_the_customer_is_already_enrolled_in_the_course() throws HoldedNotRespond, MoodleNotRespond, CustomFieldNotExists {
        var contact = mock(HoldedContact.class);
        when(contact.getEmail()).thenReturn("random_username@random_domain.com");
        var course = mock(MoodleCourse.class);
        when(course.getDisplayname()).thenReturn("random name");
        when(course.getPrice()).thenReturn(new MoodlePrice("33.4"));
        customerData.setCourseId("1");
        customerData.setDnicif("46842041C");
        customerData.setEmail("random_username@random_domain.com");
        when(moodleApiClient.getCourse("1")).thenReturn(course);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(contact);
        when(moodleApiClient.existsAnUserinThisCourse(any(), any())).thenReturn(true);

        var thrown = Assertions.assertThrows(UserIsEnroledInTheCourse.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_holded_not_respond() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, NoPriceAvailable, CustomFieldNotExists, UserIsEnroledInTheCourse, MoodleIsNotAvailable {
        var expectedCustomId = "RANDOM_CUSTOM_ID";
        var contact = mock(HoldedContact.class);
        when(contact.getEmail()).thenReturn("random_username@random_domain.com");
        var course = mock(MoodleCourse.class);
        when(course.getDisplayname()).thenReturn("RANDOM NAME");
        when(course.getPrice()).thenReturn(new MoodlePrice("44.5"));
        customerData.setCourseId("1");
        customerData.setEmail("random_username@random_domain.com");
        customerData.setDnicif("46842041C");
        when(holdedApiClient.getContactByCustomId(any())).thenThrow(HoldedNotRespond.class);
        when(moodleApiClient.getCourse("1")).thenReturn(course);
        when(holdedApiClient.createCustomId(any(), any())).thenReturn(expectedCustomId);

        var thrown = Assertions.assertThrows(HoldedIsNotAvailable.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void not_to_create_a_contact_if_the_contact_exists_in_holded() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, NoPriceAvailable, CustomFieldNotExists, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var expectedCustomId = "RANDOM_CUSTOM_ID";
        var contact = mock(HoldedContact.class);
        when(contact.getEmail()).thenReturn("random_username@random_domain.com");
        var course = mock(MoodleCourse.class);
        when(course.getDisplayname()).thenReturn("RANDOME NAME");
        when(course.getPrice()).thenReturn(new MoodlePrice("44.5"));
        customerData.setCourseId("1");
        customerData.setEmail("random_username@random_domain.com");
        customerData.setDnicif("46842041C");
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(contact);
        when(moodleApiClient.getCourse("1")).thenReturn(course);
        when(holdedApiClient.createCustomId(any(), any())).thenReturn(expectedCustomId);

        useCase.subscribe(customerData);

        verify(holdedApiClient).getContactByCustomId(expectedCustomId);
        verify(holdedApiClient, never()).createContact(any(), any(), any(), any(), any());
    }

    @Test
    void create_a_contact_with_upper_case_data_when_contact_not_exists() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, NoPriceAvailable, CustomFieldNotExists, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var expectedCustomId = "RANDOM_CUSTOM_ID";
        var course = mock(MoodleCourse.class);
        when(course.getDisplayname()).thenReturn("random course name");
        when(course.getPrice()).thenReturn(new MoodlePrice("44.3"));
        var email = "random_username@random_domain.com";
        var firstName = "John";
        var surname = "Doe";
        var companyName = "random comapny";
        var nifCif = "46842041c";
        customerData.setCourseId("1");
        customerData.setEmail(email);
        customerData.setName(firstName);
        customerData.setSurname(surname);
        customerData.setPostalCode("28080");
        customerData.setAddress("random direction");
        customerData.setPhoneNumber("916185445");
        customerData.setCompany("random company");
        customerData.setIsCompany(true);
        customerData.setRegion("random region");
        customerData.setCity("random city");
        customerData.setDnicif(nifCif);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse("1")).thenReturn(course);
        when(holdedApiClient.createCustomId(any(), any())).thenReturn(expectedCustomId);
        var contact = mock(HoldedContact.class);
        when(contact.getEmail()).thenReturn(email);
        when(holdedApiClient.createContact(any(), any(), any(), any(), any())).thenReturn(contact);

        useCase.subscribe(customerData);

        verify(holdedApiClient).getContactByCustomId(expectedCustomId);
        (holdedApiClient).createContact(
                eq(firstName.toUpperCase()),
                eq(surname.toUpperCase()),
                eq(new HoldedEmail(email)),
                eq(companyName.toUpperCase()),
                eq(nifCif.toUpperCase()));
    }

    @Test
    void create_an_invoice_with_the_course_data() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var expectedCustomId = "RANDOM_CUSTOM_ID";
        var course = mock(MoodleCourse.class);
        when(course.getId()).thenReturn(1);
        when(course.getPrice()).thenReturn(new MoodlePrice("14.5"));
        when(course.getDisplayname()).thenReturn("random_display_name");
        var email = "randomusername@randomdomain.com";
        var firstName = "John";
        var surname = "Doe";
        var nifCif = "46842041c";
        customerData.setCourseId("1");
        customerData.setEmail(email);
        customerData.setName(firstName);
        customerData.setSurname(surname);
        customerData.setPostalCode("28080");
        customerData.setAddress("random direction");
        customerData.setPhoneNumber("916185445");
        customerData.setCompany("random company");
        customerData.setIsCompany(true);
        customerData.setRegion("random region");
        customerData.setCity("random city");
        customerData.setDnicif(nifCif);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse("1")).thenReturn(course);
        when(holdedApiClient.createCustomId(any(), any())).thenReturn(expectedCustomId);
        var contact = mock(HoldedContact.class);
        when(contact.getEmail()).thenReturn(email);
        var invoice = new HoldedCreationDataInvoice();
        when(holdedApiClient.createContact(any(), any(), any(), any(), any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);

        useCase.subscribe(customerData);

        verify(holdedApiClient).createInvoice(
                any(),
                eq("random_display_name"),
                eq(""),
                eq(1),
                eq(14.5)
        );
    }

    @Test
    void throw_an_exception_when_price_not_exist_in_moodle() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var expectedCustomId = "RANDOM_CUSTOM_ID";
        var course = mock(MoodleCourse.class);
        when(course.getId()).thenReturn(1);
        when(course.getDisplayname()).thenReturn("random_display_name");
        when(course.getPrice()).thenThrow(CustomFieldNotExists.class);
        var email = "random_username@random_domain.com";
        var firstName = "John";
        var surname = "Doe";
        var nifCif = "46842041c";
        customerData.setCourseId("1");
        customerData.setEmail(email);
        customerData.setName(firstName);
        customerData.setSurname(surname);
        customerData.setPostalCode("28080");
        customerData.setAddress("random direction");
        customerData.setPhoneNumber("916185445");
        customerData.setCompany("random company");
        customerData.setIsCompany(true);
        customerData.setRegion("random region");
        customerData.setCity("random city");
        customerData.setDnicif(nifCif);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse("1")).thenReturn(course);
        when(holdedApiClient.createCustomId(any(), any())).thenReturn(expectedCustomId);
        var contact = mock(HoldedContact.class);
        var invoice = new HoldedCreationDataInvoice();
        when(holdedApiClient.createContact(any(), any(), any(), any(), any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);

        var thrown = Assertions.assertThrows(NoPriceAvailable.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();


    }

    @Test
    void send_the_invoice_to_the_customer_email() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var expectedCustomId = "RANDOM_CUSTOM_ID";
        var course = mock(MoodleCourse.class);
        when(course.getId()).thenReturn(1);
        when(course.getPrice()).thenReturn(new MoodlePrice("14.5"));
        when(course.getDisplayname()).thenReturn("random_display_name");
        var email = "random_username@random_domain.com";
        var firstName = "John";
        var surname = "Doe";
        var nifCif = "46842041c";
        customerData.setCourseId("1");
        customerData.setEmail(email);
        customerData.setName(firstName);
        customerData.setSurname(surname);
        customerData.setPostalCode("28080");
        customerData.setAddress("random direction");
        customerData.setPhoneNumber("916185445");
        customerData.setCompany("random company");
        customerData.setIsCompany(true);
        customerData.setRegion("random region");
        customerData.setCity("random city");
        customerData.setDnicif(nifCif);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse("1")).thenReturn(course);
        when(holdedApiClient.createCustomId(any(), any())).thenReturn(expectedCustomId);
        var contact = mock(HoldedContact.class);
        when(contact.getEmail()).thenReturn(email);
        var invoice = new HoldedCreationDataInvoice();
        ArgumentCaptor<List<HoldedEmail>> captor = ArgumentCaptor.forClass(List.class);
        when(holdedApiClient.createContact(any(), any(), any(), any(), any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);
        var status = mock(HoldedStatus.class);
        when(holdedApiClient.sendInvoice(any(), captor.capture())).thenReturn(status);

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
    void enrolle_a_new_user_in_moodle() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var expectedCustomId = "RANDOM_CUSTOM_ID";
        var course = mock(MoodleCourse.class);
        when(course.getId()).thenReturn(1);
        when(course.getPrice()).thenReturn(new MoodlePrice("14.5"));
        when(course.getDisplayname()).thenReturn("random_display_name");
        var email = "random_username@random_domain.com";
        var firstName = "John";
        var surname = "Doe";
        var nifCif = "46842041c";
        customerData.setCourseId("1");
        customerData.setEmail(email);
        customerData.setName(firstName);
        customerData.setSurname(surname);
        customerData.setPostalCode("28080");
        customerData.setAddress("random direction");
        customerData.setPhoneNumber("916185445");
        customerData.setCompany("random company");
        customerData.setIsCompany(true);
        customerData.setRegion("random region");
        customerData.setCity("random city");
        customerData.setDnicif(nifCif);

        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse("1")).thenReturn(course);
        when(holdedApiClient.createCustomId(any(), any())).thenReturn(expectedCustomId);
        when(moodleApiClient.getUserByMail(email)).thenReturn(null);
        var contact = mock(HoldedContact.class);
        when(contact.getEmail()).thenReturn(email);
        var invoice = new HoldedCreationDataInvoice();
        when(holdedApiClient.createContact(any(), any(), any(), any(), any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);
        var user = mock(MoodleUser.class);
        when(user.getId()).thenReturn("1");
        when(user.getEmail()).thenReturn(email);
        var captorCourse = ArgumentCaptor.forClass(MoodleCourse.class);
        var captorUser = ArgumentCaptor.forClass(MoodleUser.class);
        when(moodleApiClient.createUser(any(), any(), any())).thenReturn(user);
        var status = mock(HoldedStatus.class);
        when(holdedApiClient.sendInvoice(any(), any())).thenReturn(status);

        useCase.subscribe(customerData);

        verify(moodleApiClient).createUser(
                customerData.getName(),
                customerData.getSurname(),
                customerData.getEmail()
        );

        verify(moodleApiClient).enrolToTheCourse(
                captorCourse.capture(),
                captorUser.capture()
        );

        var enrolledUser = captorUser.getValue();
        var enrolleCourse = captorCourse.getValue();
        assertThat(enrolleCourse.getId()).isEqualTo(course.getId());
        assertThat(enrolledUser.getId()).isEqualTo("1");
        assertThat(enrolledUser.getEmail()).isEqualTo(email);
    }

    @Test
    void enrolle_an_existent_user_in_moodle() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var expectedCustomId = "RANDOM_CUSTOM_ID";
        var course = mock(MoodleCourse.class);
        when(course.getId()).thenReturn(1);
        when(course.getPrice()).thenReturn(new MoodlePrice("14.5"));
        when(course.getDisplayname()).thenReturn("random_display_name");
        var email = "random_username@random_domain.com";
        var firstName = "John";
        var surname = "Doe";
        var nifCif = "46842041c";
        customerData.setCourseId("1");
        customerData.setEmail(email);
        customerData.setName(firstName);
        customerData.setSurname(surname);
        customerData.setPostalCode("28080");
        customerData.setAddress("random direction");
        customerData.setPhoneNumber("916185445");
        customerData.setCompany("random company");
        customerData.setIsCompany(true);
        customerData.setRegion("random region");
        customerData.setCity("random city");
        customerData.setDnicif(nifCif);
        var user = mock(MoodleUser.class);
        when(user.getId()).thenReturn("1");
        when(user.getEmail()).thenReturn(email);

        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse("1")).thenReturn(course);
        when(holdedApiClient.createCustomId(any(), any())).thenReturn(expectedCustomId);
        when(moodleApiClient.getUserByMail(email)).thenReturn(user);
        var contact = mock(HoldedContact.class);
        when(contact.getEmail()).thenReturn(email);
        var invoice = new HoldedCreationDataInvoice();
        when(holdedApiClient.createContact(any(), any(), any(), any(), any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);

        var captorCourse = ArgumentCaptor.forClass(MoodleCourse.class);
        var captorUser = ArgumentCaptor.forClass(MoodleUser.class);
        var status = mock(HoldedStatus.class);
        when(holdedApiClient.sendInvoice(any(), any())).thenReturn(status);

        useCase.subscribe(customerData);

        verify(moodleApiClient, never()).createUser(
                any(),
                any(),
                any()
        );

        verify(moodleApiClient).enrolToTheCourse(
                captorCourse.capture(),
                captorUser.capture()
        );

        var enrolledUser = captorUser.getValue();
        var enrolleCourse = captorCourse.getValue();
        assertThat(enrolleCourse.getId()).isEqualTo(course.getId());
        assertThat(enrolledUser.getId()).isEqualTo("1");
        assertThat(enrolledUser.getEmail()).isEqualTo(email);
    }
}
