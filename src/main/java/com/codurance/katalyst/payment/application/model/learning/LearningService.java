package com.codurance.katalyst.payment.application.model.learning;

import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.model.learning.entity.Course;
import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import org.springframework.stereotype.Service;

@Service
public class LearningService {
    private MoodleApiClient moodleApiClient;

    public LearningService(MoodleApiClient moodleApiClient) {
        this.moodleApiClient = moodleApiClient;
    }

    public boolean acquireACourseFor(Purchase purchase) {
        throw new UnsupportedOperationException();
    }

    public Course getCourse(String courseId) throws LearningPlatformIsNotAvailable, NoPriceAvailable {
        try {
            var moodleCourse = this.moodleApiClient.getCourse(courseId);
            if (moodleCourse == null) {
                return null;
            }

            var moodlePrice = moodleCourse.getPrice();
            return new Course(
                    moodleCourse.getId(),
                    moodleCourse.getDisplayname(),
                    moodlePrice.getValue()
            );
        } catch (CustomFieldNotExists exception) {
            throw new NoPriceAvailable();
        } catch (MoodleNotRespond exception) {
            throw new LearningPlatformIsNotAvailable();
        }
    }

    public boolean isThereASeatFor(String courseId, String email) throws LearningPlatformIsNotAvailable {
        try {
            return !moodleApiClient.existsAnUserinThisCourse(courseId, email);
        } catch (MoodleNotRespond exception) {
            throw new LearningPlatformIsNotAvailable();
        }
    }
}
