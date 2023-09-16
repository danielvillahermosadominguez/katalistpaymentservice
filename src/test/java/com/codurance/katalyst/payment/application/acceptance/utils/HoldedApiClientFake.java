package com.codurance.katalyst.payment.application.acceptance.utils;

import com.codurance.katalyst.payment.application.HoldedApiClient;
import com.codurance.katalyst.payment.application.holded.HoldedContactDTO;
import com.codurance.katalyst.payment.application.holded.HoldedInvoiceDTO;
import com.codurance.katalyst.payment.application.utils.Mail;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoldedApiClientFake implements HoldedApiClient {

    class HoldedContactDTOFake extends HoldedContactDTO {
        public static int idCounter = 0;

        public HoldedContactDTOFake(String customId, String name, String surname, String email, String company, String nifCif) throws UnsupportedEncodingException {
            super();
            this.name = name + " " + surname + "(" + company + ")";
            this.email = email;
            this.code = nifCif;
            this.id = ++idCounter + "";
            this.customId = customId;
        }
    }

    class HoldedInvoiceDTOFake extends HoldedInvoiceDTO {
        public static int idCounter = 0;

        public HoldedInvoiceDTOFake() {
            super();
            this.id = ++idCounter + "";
        }
    }

    private Map<String, HoldedContactDTO> contacts = new HashMap<>();

    private Map<String, List<HoldedInvoiceDTO>> sentInvoices = new HashMap<>();

    @Override
    public HoldedContactDTO createContact(String name, String surname, String email, String company, String dnicif) throws UnsupportedEncodingException {
        String customId = createCustomId(dnicif, email);
        HoldedContactDTO holdedContact = new HoldedContactDTOFake(customId, name, surname, email, company, dnicif);
        this.contacts.put(customId, holdedContact);
        return holdedContact;
    }

    @Override
    public HoldedContactDTO getContactByCustomId(String customId) {
        if (!contacts.containsKey(customId)) {
            return null;
        }
        return contacts.get(customId);
    }

    @Override
    public HoldedInvoiceDTO createInvoice(HoldedContactDTO contact, String concept, String description, int amount, double price) {
        return new HoldedInvoiceDTOFake();
    }

    @Override
    public void sendInvoice(HoldedInvoiceDTO invoice, String emails) {
        List<HoldedInvoiceDTO> sentList;
        if (!sentInvoices.containsKey(emails)) {
            sentList = new ArrayList<>();
            sentInvoices.put(emails, sentList);
        }
        sentList = sentInvoices.get(emails);
        sentList.add(invoice);
    }

    @Override
    public String createCustomId(String nifCif, String email) throws UnsupportedEncodingException {
        Mail mail = new Mail(email);
        String customId = nifCif + mail.getInUnicodeFormat();
        return customId;
    }

    public void reset() {
        this.contacts.clear();
    }

    public List<HoldedInvoiceDTO> getSentInvoices(String emails) {
        List<HoldedInvoiceDTO> sentList;
        if (!sentInvoices.containsKey(emails)) {
            return Arrays.asList();
        }
        sentList = sentInvoices.get(emails);
        return sentList;
    }
}
