package com.codurance.katalyst.payment.application.actions;

import com.codurance.katalyst.payment.application.model.learning.entity.Course;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObtainTheCourse {
    private MoodleApiClient moodleApiClient;

    @Autowired
    public ObtainTheCourse(MoodleApiClient moodleApiClient) {
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
