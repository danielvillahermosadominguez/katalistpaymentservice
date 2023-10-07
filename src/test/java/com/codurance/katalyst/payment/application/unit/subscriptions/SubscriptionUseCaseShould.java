package com.codurance.katalyst.payment.application.unit.subscriptions;

import com.codurance.katalyst.payment.application.api.PotentialCustomerData;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.ports.Holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedTypeContact;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.HoldedNotRespond;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.NotValidEMailFormat;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.io.UnsupportedEncodingException;
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
    private PotentialCustomerData customerData;

    @BeforeEach
    void beforeEach() {
        holdedApiClient = mock(HoldedApiClient.class);
        moodleApiClient = mock(MoodleApiClient.class);
        payCometApiClient = mock(PayCometApiClient.class);
        dateService = mock(DateService.class);
        useCase = new SubscriptionUseCase(
                holdedApiClient,
                moodleApiClient,
                payCometApiClient,
                dateService
        );
        customerData = new PotentialCustomerData();
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
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void throw_an_exception_if_customer_data_dont_have_tpv_token(String token) {
        customerData.setCourseId("1");
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
        customerData.setCourseId("1");
        when(moodleApiClient.getCourse(any())).thenThrow(MoodleNotRespond.class);
        var thrown = assertThrows(MoodleIsNotAvailable.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_the_course_exists_in_moodle() {
        customerData.setCourseId("1");

        var thrown = assertThrows(CourseNotExists.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_the_email_is_not_valid() throws MoodleNotRespond, CustomFieldNotExists {
        var courseId = 1;
        var courseIdString = String.format("%s", courseId);
        customerData.setCourseId(courseIdString);
        customerData.setEmail("RANDOM_NO_VALID_EMAIL");
        var moodleCourse = mock(MoodleCourse.class);
        when(moodleCourse.getDisplayname()).thenReturn("RANDOM DISPLAY NAME");
        when(moodleCourse.getId()).thenReturn(courseId);
        when(moodleCourse.getPrice()).thenReturn(new MoodlePrice("100"));
        when(moodleApiClient.getCourse(courseIdString)).thenReturn(moodleCourse);

        var thrown = assertThrows(InvalidInputCustomerData.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void check_if_the_course_exists_in_moodle() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NoPriceAvailable, CustomFieldNotExists, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid, NotValidEMailFormat {
        var courseId = "1";
        var course = mock(MoodleCourse.class);
        var email = new HoldedEmail("random_username@random_domain.com");
        var nifCif = "46842041C";
        HoldedContact contact = createBasicContact(email, nifCif);

        customerData.setCourseId(courseId);
        customerData.setDnicif(nifCif);
        customerData.setEmail(email.getValue());
        when(course.getDisplayname()).thenReturn("random name");
        when(course.getPrice()).thenReturn(
                new MoodlePrice("33.4")
        );
        when(moodleApiClient.getCourse(courseId)).thenReturn(course);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(contact);

        useCase.subscribe(customerData);

        verify(moodleApiClient).getCourse(courseId);
    }

    @Test
    void throw_an_exception_if_the_customer_is_already_enrolled_in_the_course() throws HoldedNotRespond, MoodleNotRespond, CustomFieldNotExists, NotValidEMailFormat {
        var courseId = "1";
        var email = new HoldedEmail("random_username@random_domain.com");
        var nifCif = "46842041C";
        var contact = createBasicContact(email, nifCif);
        var course = mock(MoodleCourse.class);
        when(course.getDisplayname()).thenReturn("random name");
        when(course.getPrice()).thenReturn(new MoodlePrice("33.4"));
        customerData.setCourseId(courseId);
        customerData.setDnicif(nifCif);
        customerData.setEmail(email.getValue());
        when(moodleApiClient.getCourse(courseId)).thenReturn(course);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(contact);
        when(moodleApiClient.existsAnUserinThisCourse(any(), any())).thenReturn(true);

        var thrown = assertThrows(UserIsEnroledInTheCourse.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_holded_not_respond() throws HoldedNotRespond, MoodleNotRespond, NotValidEMailFormat, CustomFieldNotExists {
        var courseId = "1";
        var nifCif = "46842041C";
        var email = new HoldedEmail("random_username@random_domain.com");
        var course = mock(MoodleCourse.class);
        customerData.setCourseId(courseId);
        customerData.setEmail(email.getValue());
        customerData.setDnicif(nifCif);
        when(course.getDisplayname()).thenReturn("RANDOM NAME");
        when(course.getPrice()).thenReturn(new MoodlePrice("44.5"));
        when(holdedApiClient.getContactByCustomId(any())).thenThrow(HoldedNotRespond.class);
        when(moodleApiClient.getCourse(courseId)).thenReturn(course);

        var thrown = assertThrows(HoldedIsNotAvailable.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void not_to_create_a_contact_if_the_contact_exists_in_holded() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, NoPriceAvailable, CustomFieldNotExists, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var email = new HoldedEmail("random_username@random_domain.com");
        var nifCif = "46842041C";
        var contact = createBasicContact(email, nifCif);
        var expectedCustomId = HoldedContact.buildCustomId(nifCif.toUpperCase(), email);
        var course = mock(MoodleCourse.class);
        var courseId = "1";
        customerData.setCourseId(courseId);
        customerData.setEmail(email.getValue());
        customerData.setDnicif(nifCif);
        when(course.getDisplayname()).thenReturn("RANDOME NAME");
        when(course.getPrice()).thenReturn(new MoodlePrice("44.5"));
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(contact);
        when(moodleApiClient.getCourse(courseId)).thenReturn(course);

        useCase.subscribe(customerData);

        verify(holdedApiClient).getContactByCustomId(expectedCustomId);
        verify(holdedApiClient, never()).createContact(any());
    }

    @Test
    void create_a_contact_with_upper_case_data_when_contact_not_exists() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, NoPriceAvailable, CustomFieldNotExists, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var courseId = "1";
        var contactArg = ArgumentCaptor.forClass(HoldedContact.class);
        var course = mock(MoodleCourse.class);
        var firstName = "John";
        var surname = "Doe";
        var companyName = "random company";
        var nifCif = "46842041c";
        var address = "random direction";
        var postalCode = "28080";
        var region = "random region";
        var city = "random city";
        var email = new HoldedEmail("random_username@random_domain.com");
        var contact = createBasicContact(email, nifCif);
        customerData.setCourseId(courseId);
        customerData.setEmail(email.getValue());
        customerData.setName(firstName);
        customerData.setSurname(surname);
        customerData.setPostalCode(postalCode);
        customerData.setAddress(address);
        customerData.setPhoneNumber("916185445");
        customerData.setCompany(companyName);
        customerData.setIsCompany(true);
        customerData.setRegion(region);
        customerData.setCity(city);
        customerData.setDnicif(nifCif);
        var expectedCustomId = HoldedContact.buildCustomId(nifCif.toUpperCase(), email);
        when(course.getDisplayname()).thenReturn("random course name");
        when(course.getPrice()).thenReturn(new MoodlePrice("44.3"));
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse(courseId)).thenReturn(course);
        when(holdedApiClient.createContact(any())).thenReturn(contact);

        useCase.subscribe(customerData);

        verify(holdedApiClient).getContactByCustomId(expectedCustomId);
        verify(holdedApiClient).createContact(contactArg.capture());
        contact = contactArg.getValue();
        assertThat(contact.getName()).isEqualTo(companyName.toUpperCase());
        assertThat(contact.getCode()).isNull();
        ;
        assertThat(contact.getVatNumber()).isEqualTo(nifCif.toUpperCase());
        assertThat(contact.getBillAddress()).isNotNull();
        var bilAddress = contact.getBillAddress();
        assertThat(bilAddress.getAddress()).isEqualTo(address.toUpperCase());
        assertThat(bilAddress.getPostalCode()).isEqualTo(postalCode.toUpperCase());
        assertThat(bilAddress.getCity()).isEqualTo(city.toUpperCase());
        assertThat(bilAddress.getProvince()).isEqualTo(region.toUpperCase());
    }

    @Test
    void create_an_invoice_with_the_course_data() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
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
        var contact = mock(HoldedContact.class);
        when(contact.getEmail()).thenReturn(new HoldedEmail(email));
        var invoice = new HoldedCreationDataInvoice();
        when(holdedApiClient.createContact(any())).thenReturn(contact);
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
    void throw_an_exception_when_price_not_exist_in_moodle() throws HoldedNotRespond, MoodleNotRespond, NotValidEMailFormat, CustomFieldNotExists {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var courseId = "1";
        var course = mock(MoodleCourse.class);
        var email = "random_username@random_domain.com";
        var firstName = "John";
        var surname = "Doe";
        var nifCif = "46842041c";
        var contact = createBasicContact(new HoldedEmail(email), nifCif);
        var invoice = new HoldedCreationDataInvoice();
        customerData.setCourseId(courseId);
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
        when(course.getId()).thenReturn(Integer.parseInt(courseId));
        when(course.getDisplayname()).thenReturn("random_display_name");
        when(course.getPrice()).thenThrow(CustomFieldNotExists.class);
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
    void send_the_invoice_to_the_customer_email() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var course = mock(MoodleCourse.class);
        when(course.getId()).thenReturn(1);
        when(course.getPrice()).thenReturn(new MoodlePrice("14.5"));
        when(course.getDisplayname()).thenReturn("random_display_name");
        var email = "random_username@random_domain.com";
        var firstName = "John";
        var surname = "Doe";
        var nifCif = "46842041c";
        var courseId = "1";
        customerData.setCourseId(courseId);
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
        var contact = mock(HoldedContact.class);
        var invoice = new HoldedCreationDataInvoice();
        ArgumentCaptor<List<HoldedEmail>> captor = ArgumentCaptor.forClass(List.class);
        when(contact.getEmail()).thenReturn(new HoldedEmail(email));
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse(courseId)).thenReturn(course);
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
    void enrolle_a_new_user_in_moodle() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var userId = "1";
        var courseId = "1";
        var course = mock(MoodleCourse.class);
        var email = "random_username@random_domain.com";
        var firstName = "John";
        var surname = "Doe";
        var nifCif = "46842041c";
        var contact = createBasicContact(new HoldedEmail(email), nifCif);
        var invoice = new HoldedCreationDataInvoice();
        var user = mock(MoodleUser.class);
        customerData.setCourseId(courseId);
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

        when(course.getId()).thenReturn(Integer.parseInt(courseId));
        when(course.getPrice()).thenReturn(new MoodlePrice("14.5"));
        when(course.getDisplayname()).thenReturn("random_display_name");
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse(courseId)).thenReturn(course);
        when(moodleApiClient.getUserByMail(email)).thenReturn(null);
        when(holdedApiClient.createContact(any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(email);
        when(moodleApiClient.createUser(any(), any(), any())).thenReturn(user);
        var captorCourse = ArgumentCaptor.forClass(MoodleCourse.class);
        var captorUser = ArgumentCaptor.forClass(MoodleUser.class);
        when(holdedApiClient.sendInvoice(any(), any())).thenReturn(new HoldedStatus());

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
        var enrolledCourse = captorCourse.getValue();
        assertThat(enrolledCourse.getId()).isEqualTo(course.getId());
        assertThat(enrolledUser.getId()).isEqualTo(userId);
        assertThat(enrolledUser.getEmail()).isEqualTo(email);
    }

    @Test
    void enrolle_an_existent_user_in_moodle() throws CourseNotExists, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        //TODO: We need to include more data in the adapter. And also, to use a dto more than a lot of parameters.
        var courseId = "1";
        var userId = "1";
        var course = mock(MoodleCourse.class);
        var email = "random_username@random_domain.com";
        var firstName = "John";
        var surname = "Doe";
        var nifCif = "46842041c";
        var user = mock(MoodleUser.class);
        var contact = createBasicContact(new HoldedEmail(email), nifCif);
        var invoice = new HoldedCreationDataInvoice();
        customerData.setCourseId(courseId);
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
        when(course.getId()).thenReturn(1);
        when(course.getPrice()).thenReturn(new MoodlePrice("14.5"));
        when(course.getDisplayname()).thenReturn("random_display_name");
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(email);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(moodleApiClient.getCourse(courseId)).thenReturn(course);
        when(moodleApiClient.getUserByMail(email)).thenReturn(user);
        when(holdedApiClient.createContact(any())).thenReturn(contact);
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
}
