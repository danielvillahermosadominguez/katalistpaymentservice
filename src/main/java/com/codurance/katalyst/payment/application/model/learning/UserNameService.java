package com.codurance.katalyst.payment.application.model.learning;

import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserNameService {
    private MoodleApiClient moodleApiClient;

    @Autowired
    public UserNameService(MoodleApiClient moodleApiClient) {
        this.moodleApiClient = moodleApiClient;
    }

    public String getAProposalForUserNameBasedOn(String userNameProposal) throws MoodleNotRespond {
        var userName = userNameProposal;
        var sufix = 1;
        while (moodleApiClient.getUserByUserName(userName) != null) {
            userName = String.format("%s%s", userNameProposal, sufix++);
        }
        return userName;
    }
}
