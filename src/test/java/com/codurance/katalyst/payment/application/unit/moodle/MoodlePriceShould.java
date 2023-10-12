package com.codurance.katalyst.payment.application.unit.moodle;

import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodlePrice;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MoodlePriceShould {
    @ParameterizedTest
    @CsvSource({
            "'A_RANDOM_TEXT',0",
            "'10',10",
            "'10.',10",
            "'10.55',10.55",
    })
    void transform_a_string_to_double_being_zero_by_default(String value, double expectedValue) {
        var sut = new MoodlePrice(value);
        assertThat(sut.getValue()).isEqualTo(expectedValue);
    }
}
