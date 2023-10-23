package com.codurance.katalyst.payment.application.model.ports.holded;

import com.codurance.katalyst.payment.application.model.ports.email.Email;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedInvoiceInfo;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.model.ports.holded.exceptions.HoldedNotRespond;

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
                             List<Email> emails) throws HoldedNotRespond;
}
