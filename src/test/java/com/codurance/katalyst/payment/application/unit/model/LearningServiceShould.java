package com.codurance.katalyst.payment.application.unit.model;

import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LearningServiceShould {

    private LearningService learningService;
    private MoodleApiClient moodleApiClient;

    @BeforeEach
    void beforeEach() {
        moodleApiClient = mock(MoodleApiClient.class);
        learningService = new LearningService(moodleApiClient);
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
    void throw_an_learning_platform_not_respond_exception_if_moodle_not_respond() throws MoodleNotRespond {
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
}
