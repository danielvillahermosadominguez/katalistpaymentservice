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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@Service
public class SubscriptionUseCase {
    private final HoldedApiClient holdedApiClient;
    private final MoodleApiClient moodleApiClient;
    private final DateService dataService;

    @Autowired
    public SubscriptionUseCase(HoldedApiClient holdedApiClient, MoodleApiClient moodleApiClient, DateService dateService) {
        this.holdedApiClient = holdedApiClient;
        this.moodleApiClient = moodleApiClient;
        this.dataService = dateService;
    }

    public void subscribe(PotentialCustomerData customerData) throws CourseNotExists, InvalidInputCustomerData, NoPriceAvailable, UserIsEnroledInTheCourse, MoodleIsNotAvailable, HoldedIsNotAvailable {
        try {
            var course = this.moodleApiClient.getCourse(customerData.getCourseId());
            if (course == null) {
                throw new CourseNotExists();
            }

            if(this.moodleApiClient.existsAnUserinThisCourse(course.getId()+"", customerData.getEmail())) {
                throw new UserIsEnroledInTheCourse();
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
    }

    private void createContactAndInvoicing(PotentialCustomerData customerData, MoodleCourse course) throws NotValidEMailFormat, UnsupportedEncodingException, HoldedNotRespond, CustomFieldNotExists {
        var email = new HoldedEmail(customerData.getEmail());
        var customId = this.holdedApiClient.createCustomId(customerData.getDnicif(), email);
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
        data.setPaytpvToken(originalData.getPaytpvToken());
        data.setUsername(originalData.getUsername());
        return holdedApiClient.createContact(data.getName(),
                data.getSurname(),
                new HoldedEmail(data.getEmail()),
                data.getCompany(),
                data.getDnicif()
        );
    }
}
