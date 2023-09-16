package com.codurance.katalyst.payment.application.ports;

import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedInvoice;

import java.io.UnsupportedEncodingException;

public interface HoldedApiClient {
    HoldedContact createContact(String name, String surname, String email, String company, String dnicif) throws UnsupportedEncodingException;

    HoldedContact getContactByCustomId(String customId);

    HoldedInvoice createInvoice(HoldedContact contact, String concept, String description, int amount, double price);

    void sendInvoice(HoldedInvoice invoice, String emails);

    String createCustomId(String nifCif, String email) throws UnsupportedEncodingException;
}
