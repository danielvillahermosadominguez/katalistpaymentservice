package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.doubles.MoodleApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestApiClient;
import com.codurance.katalyst.payment.application.apirest.dto.ErrorResponseFactory;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.learning.entity.Course;
import com.codurance.katalyst.payment.application.apirest.dto.Error;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodlePrice;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.experimental.categories.Categories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class StepdefsObtainCourseFeature {

    public static final int CODE_COURSE_DOESNT_EXIST = 1;
    public static final int CODE_COURSE_DOESNT_HAVE_PRICE = 4;
    public static final int CODE_GENERAL_PROBLEM_WITH_THE_PLATFORM = 3;
    @LocalServerPort
    int randomServerPort;
    @Autowired
    private TestApiClient apiClient;

    @Autowired
    MoodleApiClientFake moodleApiClient;
    private Course selectedCourse;

    @Before
    public void beforeEachScenario() {
        if (!apiClient.isInitialized()) {
            this.apiClient.setPort(randomServerPort);
        }
        apiClient.resetLastErrors();
        var response = this.apiClient.checkItsAlive();

        if (!response.getBody().equals("OK! Working")) {
            fail();
        }

        moodleApiClient.reset();
    }

    @Given("a set of courses availables in the learning platform")
    public void a_set_of_courses_availables_in_the_learning_platform(DataTable dataTable) {
        var availableCoursesInTableFormat = dataTable.asMaps(String.class, String.class);
        var availableCourses = createMoodleCourses(availableCoursesInTableFormat);
        for (var course : availableCourses) {
            moodleApiClient.addCourse(course);
        }
    }

    @Given("the e-learning platform is not available")
    public void the_e_learning_platform_is_not_available() {
        moodleApiClient.notAvailable();
    }

    @When("the customer open the payment system with the id {int} for the course")
    public void the_customer_open_the_payment_system_with_the_id_for_the_course(Integer courseId) {
        selectedCourse = this.apiClient.getCourse(courseId);
    }

    @Then("the customer can see the course name is {string} and the price is {double} euros")
    public void the_customer_can_see_the_course_name_is_and_the_price_is_euros(String name, Double price) {
        assertThat(selectedCourse).isNotNull();
        assertThat(selectedCourse.getName()).isEqualTo(name);
        assertThat(selectedCourse.getPrice()).isEqualTo(price);
    }

    @Then("the customer can see the course is not available because doesn't exist")
    public void the_customer_can_see_the_course_is_not_available_because_doesn_t_exist() {
        assertThat(selectedCourse).isNull();
        var courseNotExistErrors = getErrorsWith(CODE_COURSE_DOESNT_EXIST);
        assertThat(courseNotExistErrors.size()).isEqualTo(1);
    }

    @Then("the customer can see the course is not available because the course has not a price")
    public void the_customer_can_see_the_course_is_not_available_because_the_course_has_not_a_price() {
        assertThat(selectedCourse).isNull();
        var courseNotExistErrors = getErrorsWith(CODE_COURSE_DOESNT_HAVE_PRICE);
        assertThat(courseNotExistErrors.size()).isEqualTo(1);
    }

    @Then("the customer can see the course is not available because the payment platform is not available")
    public void the_customer_can_see_the_course_is_not_available_because_the_payment_platform_is_not_available() {
        assertThat(selectedCourse).isNull();
        var courseNotExistErrors = getErrorsWith(CODE_GENERAL_PROBLEM_WITH_THE_PLATFORM);
        assertThat(courseNotExistErrors.size()).isEqualTo(1);
    }

    private List<Error> getErrorsWith(int code) {
        var errors = this.apiClient.getLastErrors();
        var courseNotExistErrors = errors.stream()
                .filter(error -> error.getCode() == code)
                .collect(Collectors.toList());
        return courseNotExistErrors;
    }

    private List<MoodleCourse> createMoodleCourses(List<Map<String, String>> availableCoursesInTableFormat) {
        var expectedCourseList = availableCoursesInTableFormat
                .stream()
                .map(data -> {
                    try {
                        return convertToMoodleCourse(data);
                    } catch (CustomFieldNotExists e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return expectedCourseList;
    }

    private MoodleCourse convertToMoodleCourse(Map<String, String> data) throws CustomFieldNotExists {

        var id = data.get("ID");
        var name = data.get("NAME");
        var price = data.get("PRICE");
        int numericId = Integer.parseInt(id);
        if (price.equals("<NO PRICE AVAILABLE>")) {
            var moodleCourse = new MoodleCourse();
            moodleCourse.setDisplayName(name);
            moodleCourse.setId(numericId);
            return moodleCourse;
        }
        var moodlePrice = new MoodlePrice(price);
        return new MoodleCourse(
                numericId,
                name,
                moodlePrice
        );
    }
}
