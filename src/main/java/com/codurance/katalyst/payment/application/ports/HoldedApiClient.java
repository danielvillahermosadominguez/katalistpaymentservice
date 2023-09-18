package com.codurance.katalyst.payment.application.ports;

import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.holded.exception.HoldedNotRespond;
import com.codurance.katalyst.payment.application.holded.dto.NotValidEMailFormat;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface HoldedApiClient {
    HoldedContact createContact(String name, String surname, HoldedEmail email, String company, String nifCif) throws UnsupportedEncodingException, HoldedNotRespond, NotValidEMailFormat;

    HoldedContact getContactByCustomId(String customId) throws HoldedNotRespond;

    HoldedCreationDataInvoice createInvoice(HoldedContact contact, String concept, String description, int amount, double price) throws HoldedNotRespond;

    HoldedStatus sendInvoice(HoldedCreationDataInvoice invoice, List<HoldedEmail> emails) throws HoldedNotRespond;

    String createCustomId(String nifCif, HoldedEmail email) throws UnsupportedEncodingException, NotValidEMailFormat;
}
