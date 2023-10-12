package com.codurance.katalyst.payment.application.unit.model;

import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.model.learning.UserNameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserNameServiceShould {

    private MoodleApiClient moodleApiClient;
    private UserNameService userNameService;

    @BeforeEach
    public void
    beforeEach() {
        moodleApiClient = mock(MoodleApiClient.class);
        userNameService = new UserNameService(moodleApiClient);
    }

    @Test
    public void
    propose_the_same_name_if_it_not_exists() throws MoodleNotRespond {
        var proposedUserName = "johndoe";

        var userName = userNameService.getAProposalForUserNameBasedOn(proposedUserName);

        assertThat(userName).isEqualTo(proposedUserName);
    }

    @Test
    public void
    propose_an_username_with_an_increased_number_if_the_username_is_being_used() throws MoodleNotRespond {
        var proposedUserName = "johndoe";
        var expectedUserName = "johndoe3";
        when(moodleApiClient.getUserByUserName(any())).thenReturn(
                new MoodleUser("1",
                        "John",
                        "Doe",
                        "johndoe",
                        "john.doe@EMAIL1.COM"),
                new MoodleUser("54",
                        "John Albert",
                        "Doe",
                        "johndoe1",
                        "john.doe@EMAIL2.COM"),
                new MoodleUser("54",
                        "Jenifer John",
                        "Doe",
                        "johndoe2",
                        "john.doe@EMAIL3.COM"),
                null
        );

        var userName = userNameService.getAProposalForUserNameBasedOn(proposedUserName);

        assertThat(userName).isEqualTo(expectedUserName);
    }
}
