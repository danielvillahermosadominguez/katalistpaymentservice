package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.ports.HoldedApiClient;
import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedInvoice;
import com.codurance.katalyst.payment.application.utils.EMail;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoldedApiClientFake implements HoldedApiClient {

    class HoldedContactDTOFake extends HoldedContact {
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

    class HoldedInvoiceDTOFake extends HoldedInvoice {
        public static int idCounter = 0;

        public HoldedInvoiceDTOFake() {
            super();
            this.id = ++idCounter + "";
        }
    }

    private Map<String, HoldedContact> contacts = new HashMap<>();

    private Map<String, List<HoldedInvoice>> sentInvoices = new HashMap<>();

    @Override
    public HoldedContact createContact(String name, String surname, String email, String company, String nifCif) throws UnsupportedEncodingException {
        String customId = createCustomId(nifCif, email);
        HoldedContact holdedContact = new HoldedContactDTOFake(customId, name, surname, email, company, nifCif);
        this.contacts.put(customId, holdedContact);
        return holdedContact;
    }

    @Override
    public HoldedContact getContactByCustomId(String customId) {
        if (!contacts.containsKey(customId)) {
            return null;
        }
        return contacts.get(customId);
    }

    @Override
    public HoldedInvoice createInvoice(HoldedContact contact, String concept, String description, int amount, double price) {
        return new HoldedInvoiceDTOFake();
    }

    @Override
    public void sendInvoice(HoldedInvoice invoice, String emails) {
        List<HoldedInvoice> sentList;
        if (!sentInvoices.containsKey(emails)) {
            sentList = new ArrayList<>();
            sentInvoices.put(emails, sentList);
        }
        sentList = sentInvoices.get(emails);
        sentList.add(invoice);
    }

    @Override
    public String createCustomId(String nifCif, String email) throws UnsupportedEncodingException {
        EMail mail = new EMail(email);
        String customId = nifCif + mail.getInUnicodeFormat();
        return customId;
    }

    public void reset() {
        this.contacts.clear();
    }

    public List<HoldedInvoice> getSentInvoices(String emails) {
        List<HoldedInvoice> sentList;
        if (!sentInvoices.containsKey(emails)) {
            return Arrays.asList();
        }
        sentList = sentInvoices.get(emails);
        return sentList;
    }
}
