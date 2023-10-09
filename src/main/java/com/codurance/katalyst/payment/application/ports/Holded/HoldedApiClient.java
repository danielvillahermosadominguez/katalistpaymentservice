package com.codurance.katalyst.payment.application.ports.Holded;

import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedInvoiceInfo;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.HoldedNotRespond;

import java.util.List;

public interface HoldedApiClient {
    HoldedContact createContact(HoldedContact contact) throws HoldedNotRespond;

    HoldedContact getContactByCustomId(String customId) throws HoldedNotRespond;

    HoldedInvoiceInfo createInvoice(HoldedContact contact, String concept, String description, int amount, double price) throws HoldedNotRespond;

    HoldedStatus sendInvoice(HoldedInvoiceInfo invoice, List<HoldedEmail> emails) throws HoldedNotRespond;
}
