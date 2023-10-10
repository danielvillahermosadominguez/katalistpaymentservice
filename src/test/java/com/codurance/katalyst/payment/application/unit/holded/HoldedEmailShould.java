package com.codurance.katalyst.payment.application.unit.holded;


import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.holded.exceptions.NotValidEMailFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HoldedEmailShould {

    @ParameterizedTest
    @CsvSource({
            "'example@mail.com','example'",
            "'example_2@mail.com','example_2'",
            "'example+other@mail.com','exampleother'",
            "'example!other@mail.com','exampleother'",
            "'example#other@mail.com','exampleother'",
            "'example$other@mail.com','exampleother'",
            "'example%other@mail.com','exampleother'",
            "'example!other@mail.com','exampleother'",
            "'example&other@mail.com','exampleother'",
            "'example'other@mail.com','exampleother'",
            "'example*other@mail.com','exampleother'",
            "'example-other@mail.com','exampleother'",
            "'example/other@mail.com','exampleother'",
            "'example=other@mail.com','exampleother'",
            "'example?other@mail.com','exampleother'",
            "'a#@mail.com','a'",
    })
    void transform_a_email_to_user_name(String email, String expectedUserName) throws NotValidEMailFormat {
        var sut = new HoldedEmail(email);
        assertThat(sut.getUserName()).isEqualTo(expectedUserName);
    }

    @ParameterizedTest
    @NullSource
    @CsvSource({
            "'NO_VALID_MAIL'",
            "''",
            "#@hola.com",
            "hola@@hola.com"
    })
    void throws_an_not_valid_email_format(String email){
        var thrown = Assertions.assertThrows(NotValidEMailFormat.class, () -> {
            new HoldedEmail(email);
        });

        assertThat(thrown).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
            "'example@mail.com','example%40mail.com'",
            "'example_2@mail.com','example_2%40mail.com'",
            "'example+other@mail.com','example%2Bother%40mail.com'",
            "'example!other@mail.com','example%21other%40mail.com'",
            "'example#other@mail.com','example%23other%40mail.com'",
            "'example$other@mail.com','example%24other%40mail.com'",
            "'example%other@mail.com','example%25other%40mail.com'",
            "'example!other@mail.com','example%21other%40mail.com'",
            "'example&other@mail.com','example%26other%40mail.com'",
            "'example'other@mail.com','%27example%27other%40mail.com%27'",
            "'example*other@mail.com','example*other%40mail.com'",
            "'example-other@mail.com','example-other%40mail.com'",
            "'example/other@mail.com','example%2Fother%40mail.com'",
            "'example=other@mail.com','example%3Dother%40mail.com'",
            "'example?other@mail.com','example%3Fother%40mail.com'",
            "'a#@mail.com','a%23%40mail.com'",
    })
    void transform_a_email_to_unicode(String email, String expectedUnicodeEmail) throws NotValidEMailFormat, UnsupportedEncodingException {
        var sut = new HoldedEmail(email);
        assertThat(sut.getInUnicodeFormat()).isEqualTo(expectedUnicodeEmail);
    }

    @Test
    void convert_a_email_list_into_recipient_string() throws NotValidEMailFormat {
        var email1 = "random1@email.com";
        var first = new HoldedEmail(email1);
        var email2 = "random2@email.com";
        var second = new HoldedEmail(email2);

        var recipients = HoldedEmail.getRecipients(Arrays.asList(first,second));

        assertThat(recipients).isEqualTo(email1+";"+email2);
    }

    @Test
    void convert_a_email_list_with_one_element_into_recipient_string() throws NotValidEMailFormat {
        var email = "random1@email.com";
        var holdedEmail = new HoldedEmail(email);

        var recipients = HoldedEmail.getRecipients(Arrays.asList(holdedEmail));

        assertThat(recipients).isEqualTo(email);
    }
}
