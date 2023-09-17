package com.codurance.katalyst.payment.application.ports;

import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedInvoice;
import com.codurance.katalyst.payment.application.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.holded.exception.HoldedNotRespond;

import java.io.UnsupportedEncodingException;

public interface HoldedApiClient {
    HoldedContact createContact(String name, String surname, String email, String company, String nifCif) throws UnsupportedEncodingException, HoldedNotRespond;

    HoldedContact getContactByCustomId(String customId) throws HoldedNotRespond;

    HoldedInvoice createInvoice(HoldedContact contact, String concept, String description, int amount, double price) throws HoldedNotRespond;

    HoldedStatus sendInvoice(HoldedInvoice invoice, String emails) throws HoldedNotRespond;

    String createCustomId(String nifCif, String email) throws UnsupportedEncodingException;
}
