package com.codurance.katalyst.payment.application.model.learning;

import com.codurance.katalyst.payment.application.model.learning.entity.Course;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import org.springframework.stereotype.Service;

@Service
public class LearningService {
    public boolean acquireACourseFor(Purchase purchase) {
        throw new UnsupportedOperationException();
    }

    public Course getCourse(String courseId) {
        throw new UnsupportedOperationException();
    }

    public boolean isThereASeatFor(String courseId, String email) {
        throw new UnsupportedOperationException();
    }
}
