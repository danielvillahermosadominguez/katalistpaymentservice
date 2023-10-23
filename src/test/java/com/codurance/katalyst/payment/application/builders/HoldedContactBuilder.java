package com.codurance.katalyst.payment.application.builders;

import com.codurance.katalyst.payment.application.model.ports.email.Email;
import com.codurance.katalyst.payment.application.model.ports.email.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedBillAddress;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedTypeContact;

import java.util.Map;

public class HoldedContactBuilder {
    private HoldedContact item;

    public HoldedContactBuilder createContactDefault(Email email, String nifCif) {
        item = createBasicContact(email, nifCif);
        return this;
    }

    public HoldedContactBuilder createFromMap(Map<String, String> data) {
        item = createContactFromMap(data);
        return this;
    }

    public HoldedContact getItem() {
        return item;
    }

    private HoldedContact createBasicContact(Email email, String nifCif) {
        var contact = new HoldedContact(
                "RANDOM_NAME",
                nifCif,
                "46842041C",
                HoldedTypeContact.CLIENT,
                true,
                email,
                "RANDOM_PHONE",
                null,
                "RANDOM_PURCHASE_ACCOUNT"
        );
        return contact;
    }

    private HoldedContact createContactFromMap(Map<String, String> holdedContactData) throws NotValidEMailFormat {
        var customId = holdedContactData.get("CUSTOMER-ID");
        var name = holdedContactData.get("NAME");
        var code = holdedContactData.get("CONTACT NIF");
        var vatNumber = holdedContactData.get("VAT NUMBER");
        var thisContactIs = holdedContactData.get("THIS CONTACT IS");
        var email = holdedContactData.get("EMAIL");
        var address = holdedContactData.get("ADDRESS");
        var phoneNumber = holdedContactData.get("PHONE NUMBER");
        var postalCode = holdedContactData.get("POSTAL CODE");
        var province = holdedContactData.get("PROVINCE");
        var city = holdedContactData.get("CITY");
        var country = holdedContactData.get("COUNTRY");
        var purchaseAccount = holdedContactData.get("PURCHASE ACCOUNT");
        var billAddress = new HoldedBillAddress(address, postalCode, city, province, country);

        var isPerson = thisContactIs.equals("Person");
        if (isPerson) {
            vatNumber = null;
        } else {
            code = null;
        }
        var contact = new HoldedContact(
                name,
                code,
                vatNumber,
                HoldedTypeContact.CLIENT,
                isPerson,
                new Email(email),
                phoneNumber,
                billAddress,
                purchaseAccount
        );
        contact.setCustomId(customId);
        return contact;
    }
}
