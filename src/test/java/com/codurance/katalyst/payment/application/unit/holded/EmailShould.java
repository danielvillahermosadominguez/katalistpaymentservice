package com.codurance.katalyst.payment.application.unit.holded;


import com.codurance.katalyst.payment.application.model.ports.email.Email;
import com.codurance.katalyst.payment.application.model.ports.email.NotValidEMailFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class EmailShould {

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
        var sut = new Email(email);
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
            new Email(email);
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
    void transform_a_email_to_unicode(String email, String expectedUnicodeEmail) throws NotValidEMailFormat {
        var sut = new Email(email);
        assertThat(sut.getInUnicodeFormat()).isEqualTo(expectedUnicodeEmail);
    }

    @Test
    void convert_a_email_list_into_recipient_string() throws NotValidEMailFormat {
        var email1 = "random1@email.com";
        var first = new Email(email1);
        var email2 = "random2@email.com";
        var second = new Email(email2);

        var recipients = Email.getRecipients(Arrays.asList(first, second));

        assertThat(recipients).isEqualTo(email1+";"+email2);
    }

    @Test
    void convert_a_email_list_with_one_element_into_recipient_string() throws NotValidEMailFormat {
        var emailString = "random1@emailString.com";
        var email = new Email(emailString);

        var recipients = Email.getRecipients(Arrays.asList(email));

        assertThat(recipients).isEqualTo(emailString);
    }
}
