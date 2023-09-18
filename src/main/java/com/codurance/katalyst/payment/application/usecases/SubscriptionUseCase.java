package com.codurance.katalyst.payment.application.usecases;

import com.codurance.katalyst.payment.application.api.PotentialCustomerData;
import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.holded.dto.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.holded.exception.HoldedNotRespond;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.ports.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;
import com.codurance.katalyst.payment.application.utils.DateService;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SubscriptionUseCase {
    private final HoldedApiClient holdedApiClient;
    private final MoodleApiClient moodleApiclient;
    private final DateService dataService;

    public SubscriptionUseCase(HoldedApiClient holdedApiClient, MoodleApiClient moodleApiClient, DateService dateService) {
        this.holdedApiClient = holdedApiClient;
        this.moodleApiclient = moodleApiClient;
        this.dataService = dateService;
    }

    public void subscribe(PotentialCustomerData customerData) throws CourseNotExists, MoodleNotRespond, UnsupportedEncodingException, HoldedNotRespond, InvalidInputCustomerData, NoPriceAvailable {
        var course = this.moodleApiclient.getCourse(customerData.getCourseId());
        if (course == null) {
            throw new CourseNotExists();
        }
        try {
            createContactAndInvoicing(customerData, course);

        } catch (NotValidEMailFormat exception) {
            throw new InvalidInputCustomerData(exception.getMessage());
        } catch (CustomFieldNotExists exception) {
            throw new NoPriceAvailable();
        }
    }

    private void createContactAndInvoicing(PotentialCustomerData customerData, MoodleCourse course) throws NotValidEMailFormat, UnsupportedEncodingException, HoldedNotRespond, CustomFieldNotExists {
        var email = new HoldedEmail(customerData.getEmail());
        var customId = this.holdedApiClient.createCustomId(customerData.getDnicif(), email);
        var contact = this.holdedApiClient.getContactByCustomId(customId);
        if (contact == null) {
            contact = this.createContactInHolded(customerData);
        }
        var concept = course.getDisplayname().toUpperCase();
        var description = "";
        var amount = 1;
        var price = course.getPrice();
        var invoice = this.holdedApiClient.createInvoice(contact,
                concept,
                description,
                amount,
                price.getValue());
        this.holdedApiClient.sendInvoice(invoice, Arrays.asList(new HoldedEmail(contact.getEmail())));
    }

    private HoldedContact createContactInHolded(PotentialCustomerData originalData) throws NotValidEMailFormat, UnsupportedEncodingException, HoldedNotRespond {
        var data = new PotentialCustomerData();
        data.setCourseId(originalData.getCourseId());
        data.setEmail(originalData.getEmail());
        data.setName(originalData.getName().toUpperCase());
        data.setSurname(originalData.getSurname().toUpperCase());
        data.setCompany(originalData.getCompany().toUpperCase());
        data.setDnicif(originalData.getDnicif().toUpperCase());
        data.setIsCompany(originalData.getIsCompany());
        data.setPhoneNumber(originalData.getPhoneNumber());
        data.setAddress(originalData.getAddress().toUpperCase());
        data.setPostalCode(originalData.getPostalCode().toUpperCase());
        data.setCity(originalData.getCity().toUpperCase());
        data.setRegion(originalData.getRegion().toUpperCase());
        data.setPayCometUserId(originalData.getPayCometUserId());
        data.setTemporalPayCometToken(originalData.getTemporalPayCometToken());
        return holdedApiClient.createContact(data.getName(),
                data.getSurname(),
                new HoldedEmail(data.getEmail()),
                data.getCompany(),
                data.getDnicif()
        );
    }
}
