package com.codurance.katalyst.payment.application.unit.subscriptions;

import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.usecases.ObtainTheCourseUseCase;
import com.codurance.katalyst.payment.application.usecases.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.NoPriceAvailable;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ObtainTheCourseUseCaseShould {

    public static final String COURSE_ID = "1";
    public static final String COURSE_ID_THAT_NOT_EXIST = "2";
    public static final String COURSE_ID_FOR_COURSE_WITHOUT_PRICE = "3";
    private MoodleCourse moodleCourse;
    private MoodleApiClient moodleApiClient;
    private MoodleApiClient moodleApiClient1;
    private MoodleCourse moodleCourseWithoutPrice;
    private ObtainTheCourseUseCase obtainTheCourseUseCase;

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
        obtainTheCourseUseCase = new ObtainTheCourseUseCase(moodleApiClient);
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
    void throw_not_elearning_system_not_available_when_the_course_has_not_price() throws MoodleNotRespond {
        when(moodleApiClient.getCourse(any())).thenThrow(MoodleNotRespond.class);
        var exception = Assert.assertThrows(LearningPlatformIsNotAvailable.class, () -> {
            obtainTheCourseUseCase.getCourse("42");
        });

        assertThat(exception).isNotNull();
    }
}
