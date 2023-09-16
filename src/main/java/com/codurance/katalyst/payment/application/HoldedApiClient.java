package com.codurance.katalyst.payment.application;

import com.codurance.katalyst.payment.application.holded.HoldedContactDTO;
import com.codurance.katalyst.payment.application.holded.HoldedInvoiceDTO;

import java.io.UnsupportedEncodingException;

public interface HoldedApiClient {
    HoldedContactDTO createContact(String name, String surname, String email, String company, String dnicif) throws UnsupportedEncodingException;

    HoldedContactDTO getContactByCustomId(String customId);

    HoldedInvoiceDTO createInvoice(HoldedContactDTO contact, String concept, String description, int amount, double price);

    void sendInvoice(HoldedInvoiceDTO invoice, String emails);

    String createCustomId(String nifCif, String email) throws UnsupportedEncodingException;
}
