package com.codurance.katalyst.payment.application.ports.Holded;

import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.HoldedNotRespond;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedEmail;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface HoldedApiClient {
    HoldedContact createContact(HoldedContact contact) throws HoldedNotRespond;

    HoldedContact getContactByCustomId(String customId) throws HoldedNotRespond;

    HoldedCreationDataInvoice createInvoice(HoldedContact contact, String concept, String description, int amount, double price) throws HoldedNotRespond;

    HoldedStatus sendInvoice(HoldedCreationDataInvoice invoice, List<HoldedEmail> emails) throws HoldedNotRespond;
}
