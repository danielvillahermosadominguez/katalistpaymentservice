package com.codurance.katalyst.payment.application.usecases;

import com.codurance.katalyst.payment.application.api.PotentialCustomerData;
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.ports.PayCometApiClient;
import com.codurance.katalyst.payment.application.ports.holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedBillAddress;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedTypeContact;
import com.codurance.katalyst.payment.application.ports.holded.exceptions.HoldedNotRespond;
import com.codurance.katalyst.payment.application.ports.holded.exceptions.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.usecases.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.usecases.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.usecases.exception.HoldedIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.usecases.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.usecases.exception.UserIsEnroledInTheCourse;
import com.codurance.katalyst.payment.application.utils.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@Service
public class SubscriptionUseCase {
    private final HoldedApiClient holdedApiClient;
    private final MoodleApiClient moodleApiClient;
    private final PayCometApiClient payCometApiClient;
    private UserNameService userNameService;

    private final DateService dataService;

    @Autowired
    public SubscriptionUseCase(HoldedApiClient holdedApiClient,
                               MoodleApiClient moodleApiClient,
                               PayCometApiClient payCometApiClient,
                               DateService dateService, UserNameService userNameService) {
        this.holdedApiClient = holdedApiClient;
        this.moodleApiClient = moodleApiClient;
        this.dataService = dateService;
        this.payCometApiClient = payCometApiClient;
        this.userNameService = userNameService;
    }

    public PaymentStatus subscribe(PotentialCustomerData customerData) throws CourseNotExists, InvalidInputCustomerData, NoPriceAvailable, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        if(customerData.getPaytpvToken() == null || customerData.getPaytpvToken().trim().isEmpty()) {
            throw new TPVTokenIsRequired();
        }
        var tpvUser = this.payCometApiClient.createUser(customerData.getPaytpvToken());
        if(tpvUser == null) {
            throw new CreditCardNotValid();
        }

        try {

            var course = this.moodleApiClient.getCourse(customerData.getCourseId());
            if (course == null) {
                throw new CourseNotExists();
            }

            if (this.moodleApiClient.existsAnUserinThisCourse(course.getId() + "", customerData.getEmail())) {
                throw new UserIsEnroledInTheCourse();
            }

            var paymentStatus = this.payCometApiClient.payment(
                    course.getPrice().getValue(),
                    "EUR",
                    tpvUser.getIdUser(),
                    "1",
                    "KATALYST SUSCRIPTION",
                    customerData.getIp(),
                    tpvUser.getTokenUser()
            );
            if (paymentStatus == null) {
                throw new CreditCardNotValid();
            }

            createContactAndInvoicing(customerData, course);
            var user = moodleApiClient.getUserByMail(customerData.getEmail());
            if (user == null) {
                user = createMoodleUser(customerData);
            }
            moodleApiClient.enrolToTheCourse(course, user);
            return paymentStatus;
        } catch (NotValidEMailFormat | UnsupportedEncodingException exception) {
            throw new InvalidInputCustomerData(exception.getMessage());
        } catch (CustomFieldNotExists exception) {
            throw new NoPriceAvailable();
        } catch (MoodleNotRespond exception) {
            throw new LearningPlatformIsNotAvailable();
        } catch (HoldedNotRespond exception) {
            throw new HoldedIsNotAvailable();
        }
    }

    private MoodleUser createMoodleUser(PotentialCustomerData customerData) throws MoodleNotRespond {
        MoodleUser user;
        var email = new HoldedEmail(customerData.getEmail());
        var name = customerData.getName();
        var surname = customerData.getSurname();
        var userName = userNameService.getAProposalForUserNameBasedOn(email.getUserName());
        if (customerData.getIsCompany()) {
            surname = "";
            name = customerData.getCompany();
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

    private void createContactAndInvoicing(PotentialCustomerData customerData, MoodleCourse course) throws NotValidEMailFormat, UnsupportedEncodingException, HoldedNotRespond, CustomFieldNotExists {
        var email = new HoldedEmail(customerData.getEmail());

        var customId = HoldedContact.buildCustomId(customerData.getDnicif(), email);
        var contact = this.holdedApiClient.getContactByCustomId(customId);
        if (contact == null) {
            contact = this.createContactInHolded(customerData);
        }
        var concept = course.getDisplayname();
        var description = "";
        var amount = 1;
        var price = course.getPrice();
        var invoice = this.holdedApiClient.createInvoice(contact,
                concept,
                description,
                amount,
                price.getValue());
        this.holdedApiClient.sendInvoice(invoice, Arrays.asList(contact.getEmail()));
    }

    private HoldedContact createContactInHolded(PotentialCustomerData originalData) throws NotValidEMailFormat, UnsupportedEncodingException, HoldedNotRespond {
        var name = originalData.getCompany().toUpperCase();
        var type = HoldedTypeContact.CLIENT;
        var isPerson = !originalData.getIsCompany();
        String code = null;
        var vatNumber = originalData.getDnicif().toUpperCase();
        if (!originalData.getIsCompany()) {
            code = vatNumber;
            vatNumber = null;
            name = originalData.getName().toUpperCase() + " " + originalData.getSurname().toUpperCase();
        }
        var billingAddress = new HoldedBillAddress(
                originalData.getAddress().toUpperCase(),
                originalData.getPostalCode().toUpperCase(),
                originalData.getCity().toUpperCase(),
                originalData.getRegion().toUpperCase(),
                originalData.getCountry().toUpperCase());
        var contact = new HoldedContact(
                name,
                code,
                vatNumber,
                type,
                isPerson,
                new HoldedEmail(originalData.getEmail()),
                originalData.getPhoneNumber().toUpperCase(),
                billingAddress,
                "70500000"
        );

        return holdedApiClient.createContact(contact);
    }
}
