package com.codurance.katalyst.payment.application.model.learning;

import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.model.learning.entity.Course;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import org.springframework.stereotype.Service;

@Service
public class LearningService {
    private final UserNameService userNameService;
    private MoodleApiClient moodleApiClient;

    public LearningService(MoodleApiClient moodleApiClient, UserNameService userNameService) {
        this.moodleApiClient = moodleApiClient;
        this.userNameService = userNameService;
    }

    public boolean acquireACourseFor(Purchase purchase) throws LearningPlatformIsNotAvailable {
        try {
            var user = moodleApiClient.getUserByMail(purchase.getEmail());
            if (user == null) {
                user = createMoodleUser(purchase);
            }

            var moodleCourse = moodleApiClient.getCourse(purchase.getCourseId());
            moodleApiClient.enrolToTheCourse(moodleCourse, user);
            return true;
        } catch (MoodleNotRespond exception) {
            throw new LearningPlatformIsNotAvailable();
        }
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

    private MoodleUser createMoodleUser(Purchase purchase) throws MoodleNotRespond {
        MoodleUser user;
        var email = new HoldedEmail(purchase.getEmail()); //ERROR DEPENDENCIA
        var name = purchase.getName();
        var surname = purchase.getSurname();
        var userName = userNameService.getAProposalForUserNameBasedOn(email.getUserName());
        if (purchase.isCompany()) {
            surname = "";
            name = purchase.getCompany();
        }
        user = moodleApiClient.createUser(
                new MoodleUser(
                        name,
                        surname,
                        userName,
                        email.getValue()
                )
        );
        return user;
    }
}
