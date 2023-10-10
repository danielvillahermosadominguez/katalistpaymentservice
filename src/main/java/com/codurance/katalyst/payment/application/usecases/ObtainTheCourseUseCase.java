package com.codurance.katalyst.payment.application.usecases;

import com.codurance.katalyst.payment.application.api.Course;
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.usecases.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.NoPriceAvailable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObtainTheCourseUseCase {
    private MoodleApiClient moodleApiClient;

    @Autowired
    public ObtainTheCourseUseCase(MoodleApiClient moodleApiClient) {
        this.moodleApiClient = moodleApiClient;
    }

    public Course getCourse(String courseId) throws NoPriceAvailable, LearningPlatformIsNotAvailable {
        try {
            var moodleCourse = moodleApiClient.getCourse(courseId);
            if (moodleCourse == null) {
                return null;
            }
            return new Course(
                    moodleCourse.getId(),
                    moodleCourse.getDisplayname(),
                    moodleCourse.getPrice().getValue());
        } catch (CustomFieldNotExists exception) {
            throw new NoPriceAvailable();
        } catch (MoodleNotRespond exception) {
            throw new LearningPlatformIsNotAvailable();
        }
    }
}
