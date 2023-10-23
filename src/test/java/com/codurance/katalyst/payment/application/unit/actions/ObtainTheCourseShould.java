package com.codurance.katalyst.payment.application.unit.actions;

import com.codurance.katalyst.payment.application.model.ports.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.actions.ObtainTheCourse;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ObtainTheCourseShould {

    public static final String COURSE_ID = "1";
    public static final String COURSE_ID_THAT_NOT_EXIST = "2";
    public static final String COURSE_ID_FOR_COURSE_WITHOUT_PRICE = "3";
    private MoodleCourse moodleCourse;
    private MoodleApiClient moodleApiClient;
    private MoodleApiClient moodleApiClient1;
    private MoodleCourse moodleCourseWithoutPrice;
    private ObtainTheCourse obtainTheCourseUseCase;

    @BeforeEach
    void beforeEach() throws CustomFieldNotExists, MoodleNotRespond {
        moodleCourse = new MoodleCourse(
                1,
                "random_name",
                new MoodlePrice("43.2")
        );
        moodleApiClient = mock(MoodleApiClient.class);
        moodleApiClient1 = mock(MoodleApiClient.class);
        moodleCourseWithoutPrice = mock(MoodleCourse.class);
        when(moodleCourseWithoutPrice.getPrice()).thenThrow(CustomFieldNotExists.class);
        when(moodleCourseWithoutPrice.getDisplayname()).thenReturn("random_name");
        when(moodleCourseWithoutPrice.getId()).thenReturn(3);
        obtainTheCourseUseCase = new ObtainTheCourse(moodleApiClient);
        when(moodleApiClient.getCourse(COURSE_ID)).thenReturn(moodleCourse);
        when(moodleApiClient.getCourse(COURSE_ID_THAT_NOT_EXIST)).thenReturn(null);
        when(moodleApiClient.getCourse(COURSE_ID_FOR_COURSE_WITHOUT_PRICE)).thenReturn(moodleCourseWithoutPrice);
    }

    @Test
    void obtain_the_course_when_is_in_moodle() throws CustomFieldNotExists, MoodleNotRespond, NoPriceAvailable, LearningPlatformIsNotAvailable {

        var course = obtainTheCourseUseCase.getCourse(COURSE_ID);

        assertThat(course).isNotNull();
        assertThat(course.getId()).isEqualTo(1);
        assertThat(course.getName()).isEqualTo("random_name");
        assertThat(course.getPrice()).isEqualTo(43.2);
    }

    @Test
    void obtain_null_value_when_is_not_in_moodle() throws CustomFieldNotExists, MoodleNotRespond, NoPriceAvailable, LearningPlatformIsNotAvailable {

        var course = obtainTheCourseUseCase.getCourse(COURSE_ID_THAT_NOT_EXIST);

        assertThat(course).isNull();
    }

    @Test
    void throw_not_price_available_when_the_course_has_not_price() {
        var exception = Assert.assertThrows(NoPriceAvailable.class, () -> {
            obtainTheCourseUseCase.getCourse(COURSE_ID_FOR_COURSE_WITHOUT_PRICE);
        });

        assertThat(exception).isNotNull();
    }
    @Test
    void throw_not_price_available_when_the_course_price_is_zero() throws CustomFieldNotExists, MoodleNotRespond {
       var moodleCourse = new MoodleCourse(
                2,
                "tdd_with_zero",
                new MoodlePrice("0")
        );

        when(moodleApiClient.getCourse("2")).thenReturn(moodleCourse);

        var exception = Assert.assertThrows(NoPriceAvailable.class, () -> {
            obtainTheCourseUseCase.getCourse("2");
        });

        assertThat(exception).isNotNull();
    }

    @Test
    void throw_not_elearning_system_not_available_when_the_course_has_not_price() throws MoodleNotRespond {
        when(moodleApiClient.getCourse(any())).thenThrow(MoodleNotRespond.class);
        var exception = Assert.assertThrows(LearningPlatformIsNotAvailable.class, () -> {
            obtainTheCourseUseCase.getCourse("42");
        });

        assertThat(exception).isNotNull();
    }
}
