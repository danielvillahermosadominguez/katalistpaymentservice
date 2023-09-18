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
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.ports.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;
import com.codurance.katalyst.payment.application.usecases.CourseNotExists;
import com.codurance.katalyst.payment.application.usecases.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.usecases.NoPriceAvailable;
import com.codurance.katalyst.payment.application.usecases.SubscriptionUseCase;
import com.codurance.katalyst.payment.application.utils.DateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

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
    private DateService dateService;
    private SubscriptionUseCase useCase;
    private PotentialCustomerData customerData;

    @Captor
    private ArgumentCaptor<List<HoldedEmail>> captor;

    @BeforeEach
    void beforeEach() {
        holdedApiClient = mock(HoldedApiClient.class);
        moodleApiClient = mock(MoodleApiClient.class);
        dateService = mock(DateService.class);
        useCase = new SubscriptionUseCase(holdedApiClient, moodleApiClient, dateService);
        customerData = new PotentialCustomerData();
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
    void throw_an_exception_if_the_email_is_not_valid() throws MoodleNotRespond {
        customerData.setCourseId("1");
        customerData.setEmail("RANDOM_NO_VALID_EMAIL");
        when(moodleApiClient.getCourse("1")).thenReturn(new MoodleCourse());

        var thrown = Assertions.assertThrows(InvalidInputCustomerData.class, () -> {
            useCase.subscribe(customerData);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void check_if_the_course_exists_in_moodle() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat, MoodleNotRespond, InvalidInputCustomerData, NoPriceAvailable, CustomFieldNotExists {
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
    void not_to_create_a_contact_if_the_contact_exists_in_holded() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, NoPriceAvailable, CustomFieldNotExists {
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
    void create_a_contact_with_upper_case_data_when_contact_not_exists() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, NoPriceAvailable, CustomFieldNotExists {
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
    void create_an_invoice_with_the_course_data() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable {
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
                eq("RANDOM_DISPLAY_NAME"),
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
    void send_the_invoice_to_the_customer_email() throws CourseNotExists, UnsupportedEncodingException, HoldedNotRespond, MoodleNotRespond, InvalidInputCustomerData, NotValidEMailFormat, CustomFieldNotExists, NoPriceAvailable {
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
}
