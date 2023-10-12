package com.codurance.katalyst.payment.application.apirest.courses;

import com.codurance.katalyst.payment.application.apirest.payment.dto.Error;
import com.codurance.katalyst.payment.application.actions.ObtainTheCourse;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CourseController {
    @Autowired
    private ObtainTheCourse obtainTheCourseUseCase;

    @GetMapping(value = "/courses/{id}")
    @ResponseBody
    public ResponseEntity<?> getCourse(@PathVariable("id") String id) {
        try {
            var course = obtainTheCourseUseCase.getCourse(id);
            if (course == null) {
                return new ResponseEntity<>(
                        new Error(
                                Error.ERROR_CODE_COURSE_DOESNT_EXIST,
                                "The course with the id " + id + " doesn't exists"
                        ),
                        HttpStatus.BAD_REQUEST
                );
            }

            return new ResponseEntity<>(
                    course,
                    HttpStatus.OK
            );
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "It has been not possible to get the course. Please try to connect later"
            );
        } catch (NoPriceAvailable e) {
            return new ResponseEntity<>(
                    new Error(
                            Error.CODE_ERROR_PRICE_NOT_FOUND,
                            "Price custom field not found in Moodle. Please, contact with the administrator to create this custom field"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        } catch (LearningPlatformIsNotAvailable e) {
            return new ResponseEntity<>(
                    new Error(Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                            "We have had a problem with the creation of the contact and the invoicing"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
