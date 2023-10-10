package com.codurance.katalyst.payment.application.ports.holded;

import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedInvoiceInfo;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.ports.holded.exceptions.HoldedNotRespond;

import java.util.List;

public interface HoldedApiClient {
    HoldedContact createContact(HoldedContact contact) throws HoldedNotRespond;

    HoldedContact getContactByCustomId(String customId) throws HoldedNotRespond;

    HoldedInvoiceInfo createInvoice(HoldedContact contact,
                                    String concept,
                                    String description,
                                    int amount,
                                    double price) throws HoldedNotRespond;

    HoldedStatus sendInvoice(HoldedInvoiceInfo invoice,
                             List<HoldedEmail> emails) throws HoldedNotRespond;
}
