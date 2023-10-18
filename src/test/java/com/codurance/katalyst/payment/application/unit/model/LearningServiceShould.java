package com.codurance.katalyst.payment.application.unit.model;

import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.builders.PurchaseBuilder;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.learning.UserNameService;
import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LearningServiceShould {

    private LearningService learningService;
    private UserNameService userNameService;
    private MoodleApiClient moodleApiClient;

    private PurchaseBuilder purchaseBuilder = new PurchaseBuilder();


    @BeforeEach
    void beforeEach() {
        moodleApiClient = mock(MoodleApiClient.class);
        userNameService = mock(UserNameService.class);
        learningService = new LearningService(moodleApiClient, userNameService);
    }

    @Test
    void obtain_a_course_if_the_course_exists_in_moodle() throws MoodleNotRespond, CustomFieldNotExists, NoPriceAvailable, LearningPlatformIsNotAvailable {
        var courseId = "1";
        var price = new MoodlePrice("14.5");
        var moodleCourse = new MoodleCourse(
                Integer.parseInt(courseId),
                "RANDOM_NAME",
                price
        );
        when(moodleApiClient.getCourse(courseId)).thenReturn(moodleCourse);

        var course = learningService.getCourse(courseId);

        verify(moodleApiClient).getCourse(courseId);
        assertThat(course).isNotNull();
        assertThat(course.getId()).isEqualTo(moodleCourse.getId());
        assertThat(course.getName()).isEqualTo(moodleCourse.getDisplayname());
        assertThat(course.getPrice()).isEqualTo(moodleCourse.getPrice().getValue());
    }

    @Test
    void obtain_a_null_value_if_the_course_not_exists_in_moodle() throws MoodleNotRespond, NoPriceAvailable, LearningPlatformIsNotAvailable {
        var courseId = "1";
        when(moodleApiClient.getCourse(courseId)).thenReturn(null);

        var course = learningService.getCourse(courseId);

        verify(moodleApiClient).getCourse(courseId);
        assertThat(course).isNull();
    }

    @Test
    void throw_an_learning_platform_not_respond_exception_if_moodle_not_respond_when_get_a_course() throws MoodleNotRespond {
        when(moodleApiClient.getCourse(any())).thenThrow(MoodleNotRespond.class);
        var exception = Assertions.assertThrows(LearningPlatformIsNotAvailable.class, () -> {
            learningService.getCourse("1");
        });

        assertThat(exception).isNotNull();
    }

    @Test
    void throw_an_exception_when_price_not_exist_in_moodle() throws MoodleNotRespond, CustomFieldNotExists {
        var courseId = "1";
        var moodleCourse = mock(MoodleCourse.class);
        when(moodleCourse.getId()).thenReturn(Integer.parseInt(courseId));
        when(moodleCourse.getDisplayname()).thenReturn("RANDOM_NAME");
        when(moodleCourse.getPrice()).thenThrow(CustomFieldNotExists.class);
        when(moodleApiClient.getCourse(courseId)).thenReturn(moodleCourse);

        var thrown = assertThrows(NoPriceAvailable.class, () -> {
            learningService.getCourse(courseId);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void respond_false_if_a_user_with_the_same_email_is_enrolled_in_the_course() throws MoodleNotRespond, LearningPlatformIsNotAvailable {
        var courseId = "1";
        var email = "random_email@email.com";
        when(moodleApiClient.existsAnUserinThisCourse(courseId, email)).thenReturn(true);

        var isThereASeat = learningService.isThereASeatFor(courseId, email);


        assertThat(isThereASeat).isFalse();
    }

    @Test
    void respond_true_if_a_user_with_the_same_email_is_not_enrolled_in_the_course() throws MoodleNotRespond, LearningPlatformIsNotAvailable {
        var courseId = "1";
        var email = "random_email@email.com";
        when(moodleApiClient.existsAnUserinThisCourse(courseId, email)).thenReturn(false);

        var isThereASeat = learningService.isThereASeatFor(courseId, email);

        assertThat(isThereASeat).isTrue();
    }

    @Test
    void throw_an_learning_platform_not_respond_exception_if_moodle_not_respond_looking_for_a_seal() throws MoodleNotRespond {
        when(moodleApiClient.existsAnUserinThisCourse(anyString(), anyString())).thenThrow(MoodleNotRespond.class);
        var exception = Assertions.assertThrows(LearningPlatformIsNotAvailable.class, () -> {
            learningService.isThereASeatFor("1", "random_email@email.com");
        });

        assertThat(exception).isNotNull();
    }

    @Test
    void create_a_new_user_for_person_in_moodle_when_not_exists() throws MoodleNotRespond, LearningPlatformIsNotAvailable {
        var purchase = purchaseBuilder
                .createWithDefaultValues()
                .getItem();
        var username = "random_username_propose_for_service";
        when(userNameService.getAProposalForUserNameBasedOn(any())).thenReturn(username);
        var userId = "1";
        var name = purchase.getName();
        var email = purchase.getEmail();
        var surname = purchase.getSurname();
        var student = new MoodleUser(
                userId,
                name,
                surname,
                username,
                email
        );
        when(moodleApiClient.getUserByMail(email)).thenReturn(null);
        when(moodleApiClient.createUser(any())).thenReturn(student);

        var result = learningService.acquireACourseFor(purchase);

        verify(moodleApiClient).createUser(
                new MoodleUser(
                        name,
                        surname,
                        username,
                        email
                )
        );
        assertThat(result).isTrue();
    }

    @Test
    void create_a_new_user_for_company_in_moodle_when_not_exists() throws MoodleNotRespond, LearningPlatformIsNotAvailable {
        var purchase = purchaseBuilder
                .createWithDefaultValues()
                .getItem();
        purchase.setIsCompany(true);
        purchase.setCompany("RANDOM_COMPANY_NAME");
        purchase.setName("N/A");
        purchase.setSurname("N/A");
        var username = "random_username_propose_for_service";
        when(userNameService.getAProposalForUserNameBasedOn(any())).thenReturn(username);
        var userId = "1";
        var name = purchase.getCompany();
        var email = purchase.getEmail();
        var surname = purchase.getCompany();
        var student = new MoodleUser(
                userId,
                name,
                surname,
                username,
                email
        );
        when(moodleApiClient.getUserByMail(email)).thenReturn(null);
        when(moodleApiClient.createUser(any())).thenReturn(student);

        var result = learningService.acquireACourseFor(purchase);

        verify(moodleApiClient).createUser(
                new MoodleUser(
                        name,
                        surname,
                        username,
                        email
                )
        );
        assertThat(result).isTrue();
    }

    @Test
    void not_to_create_a_new_user_in_moodle_when_exists() throws MoodleNotRespond, LearningPlatformIsNotAvailable {
        var purchase = purchaseBuilder
                .createWithDefaultValues()
                .getItem();
        var username = "random_username_propose_for_service";
        when(userNameService.getAProposalForUserNameBasedOn(any())).thenReturn(username);
        var userId = "1";
        var name = purchase.getName();
        var email = purchase.getEmail();
        var surname = purchase.getSurname();
        var student = new MoodleUser(
                userId,
                name,
                surname,
                username,
                email
        );
        when(moodleApiClient.getUserByMail(email)).thenReturn(student);

        var result = learningService.acquireACourseFor(purchase);

        verify(moodleApiClient, never()).createUser(any());
        assertThat(result).isTrue();
    }

    @Test
    void enrolle_a_new_user_in_moodle_when_is_person() throws MoodleNotRespond, LearningPlatformIsNotAvailable, CustomFieldNotExists {
        var purchase = purchaseBuilder
                .createWithDefaultValues()
                .getItem();
        var username = "random_username_propose_for_service";
        when(userNameService.getAProposalForUserNameBasedOn(any())).thenReturn(username);
        var userId = "1";
        var courseId = Integer.parseInt(purchase.getCourseId());
        var name = purchase.getName();
        var email = purchase.getEmail();
        var surname = purchase.getName();
        var student = new MoodleUser(
                userId,
                name,
                surname,
                username,
                email
        );

        var course = new MoodleCourse(Integer.parseInt(courseId + ""),
                purchase.getConcept(),
                new MoodlePrice(purchase.getPrice() + "")
        );

        when(moodleApiClient.getCourse(courseId + "")).thenReturn(course);
        when(moodleApiClient.getUserByMail(email)).thenReturn(student);
        var captorCourse = ArgumentCaptor.forClass(MoodleCourse.class);
        var captorUser = ArgumentCaptor.forClass(MoodleUser.class);

        var result = learningService.acquireACourseFor(purchase);

        verify(moodleApiClient).enrolToTheCourse(
                captorCourse.capture(),
                captorUser.capture()
        );

        var enrolledUser = captorUser.getValue();
        var enrolledCourse = captorCourse.getValue();
        assertThat(enrolledCourse.getId()).isEqualTo(courseId);
        assertThat(enrolledUser.getId()).isEqualTo(userId);
        assertThat(enrolledUser.getName()).isEqualTo(name);
        assertThat(enrolledUser.getLastName()).isEqualTo(surname);
        assertThat(enrolledUser.getUserName()).isEqualTo(username);
        assertThat(enrolledUser.getEmail()).isEqualTo(email);
        assertThat(result).isTrue();
    }

    @Test
    void enrolle_a_new_user_in_moodle_when_is_company() throws MoodleNotRespond, LearningPlatformIsNotAvailable, CustomFieldNotExists {
        var purchase = purchaseBuilder
                .createWithDefaultValues()
                .getItem();
        purchase.setIsCompany(true);
        purchase.setCompany("RANDOM_COMPANY_NAME");
        purchase.setName("N/A");
        purchase.setSurname("N/A");
        var username = "random_username_propose_for_service";
        when(userNameService.getAProposalForUserNameBasedOn(any())).thenReturn(username);
        var userId = "1";
        var courseId = Integer.parseInt(purchase.getCourseId());
        var name = purchase.getName();
        var email = purchase.getEmail();
        var surname = "";
        var student = new MoodleUser(
                userId,
                name,
                surname,
                username,
                email
        );

        var course = new MoodleCourse(Integer.parseInt(courseId + ""),
                purchase.getConcept(),
                new MoodlePrice(purchase.getPrice() + "")
        );

        when(moodleApiClient.getCourse(courseId + "")).thenReturn(course);
        when(moodleApiClient.getUserByMail(email)).thenReturn(student);
        var captorCourse = ArgumentCaptor.forClass(MoodleCourse.class);
        var captorUser = ArgumentCaptor.forClass(MoodleUser.class);

        var result = learningService.acquireACourseFor(purchase);

        verify(moodleApiClient).enrolToTheCourse(
                captorCourse.capture(),
                captorUser.capture()
        );

        var enrolledUser = captorUser.getValue();
        var enrolledCourse = captorCourse.getValue();
        assertThat(enrolledCourse.getId()).isEqualTo(courseId);
        assertThat(enrolledUser.getId()).isEqualTo(userId);
        assertThat(enrolledUser.getName()).isEqualTo(name);
        assertThat(enrolledUser.getLastName()).isEqualTo(surname);
        assertThat(enrolledUser.getUserName()).isEqualTo(username);
        assertThat(enrolledUser.getEmail()).isEqualTo(email);
        assertThat(result).isTrue();
    }

    @Test
    void throw_an_learning_platform_not_respond_exception_if_moodle_not_respond_when_get_acquire_a_course() throws MoodleNotRespond {
        var purchase = purchaseBuilder
                .createWithDefaultValues()
                .getItem();
        when(moodleApiClient.getCourse(any())).thenThrow(MoodleNotRespond.class);
        var exception = Assertions.assertThrows(LearningPlatformIsNotAvailable.class, () -> {
            learningService.acquireACourseFor(purchase);
        });

        assertThat(exception).isNotNull();
    }
}
