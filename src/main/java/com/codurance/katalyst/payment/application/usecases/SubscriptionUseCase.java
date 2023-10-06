package com.codurance.katalyst.payment.application.usecases;

import com.codurance.katalyst.payment.application.api.PotentialCustomerData;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedBillAddress;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedTypeContact;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.HoldedNotRespond;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.ports.Holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;
import com.codurance.katalyst.payment.application.ports.PayCometApiClient;
import com.codurance.katalyst.payment.application.usecases.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.usecases.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.usecases.exception.HoldedIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.usecases.exception.MoodleIsNotAvailable;
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

    private final DateService dataService;

    @Autowired
    public SubscriptionUseCase(HoldedApiClient holdedApiClient,
                               MoodleApiClient moodleApiClient,
                               PayCometApiClient payCometApiClient,
                               DateService dateService) {
        this.holdedApiClient = holdedApiClient;
        this.moodleApiClient = moodleApiClient;
        this.dataService = dateService;
        this.payCometApiClient = payCometApiClient;
    }

    public PaymentStatus subscribe(PotentialCustomerData customerData) throws CourseNotExists, InvalidInputCustomerData, NoPriceAvailable, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        PaymentStatus paymentStatus = null;
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

            if(this.moodleApiClient.existsAnUserinThisCourse(course.getId()+"", customerData.getEmail())) {
                throw new UserIsEnroledInTheCourse();
            }
            paymentStatus = this.payCometApiClient.payment(
                    course.getPrice().getValue(),
                    "EUR",
                    tpvUser.getIdUser(),
                    "1",
                    "KATALYST SUSCRIPTION",
                    customerData.getIp(),
                    tpvUser.getTokenUser()
            );
            if(paymentStatus == null) {
                throw new CreditCardNotValid(); // habrá que mandar otra
            }

            createContactAndInvoicing(customerData, course);
            var user = moodleApiClient.getUserByMail(customerData.getEmail());
            if (user == null) {
                user = moodleApiClient.createUser(
                        customerData.getName(),
                        customerData.getSurname(),
                        customerData.getEmail()
                );
            }
            moodleApiClient.enrolToTheCourse(course, user);
        } catch (NotValidEMailFormat| UnsupportedEncodingException exception) {
            throw new InvalidInputCustomerData(exception.getMessage());
        } catch (CustomFieldNotExists exception) {
            throw new NoPriceAvailable();
        } catch (MoodleNotRespond exception) {
            throw new MoodleIsNotAvailable();
        } catch (HoldedNotRespond exception) {
            throw new HoldedIsNotAvailable();
        }
        return paymentStatus;
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
        if(!originalData.getIsCompany()) {
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
                originalData.getDnicif().toUpperCase(),
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
