package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoiceItem;
import com.codurance.katalyst.payment.application.ports.Holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoldedApiClientFake implements HoldedApiClient {

    public static int holdedCounterId = 0;

    class HoldedCreationDataInvoiceDTOFake extends HoldedCreationDataInvoice {
        public static int idCounter = 0;

        public HoldedCreationDataInvoiceDTOFake(List<HoldedCreationDataInvoiceItem> items) {
            super();
            this.id = ++idCounter + "";
            this.items = items;
        }
    }

    class HoldedStatusDTOFake extends HoldedStatus {
        public HoldedStatusDTOFake(int status, String info, String id) {
            this.status = status;
            this.info = info;
            this.id = id;
        }
    }

    private Map<String, HoldedContact> contacts = new HashMap<>();

    private Map<String, List<HoldedCreationDataInvoice>> sentInvoices = new HashMap<>();

    @Override
    public HoldedContact createContact(HoldedContact contact) {
        contact.setId(++holdedCounterId + "");
        this.contacts.put(contact.getCustomId(), contact);
        return contact;
    }

    @Override
    public HoldedContact getContactByCustomId(String customId) {
        if (!contacts.containsKey(customId)) {
            return null;
        }
        return contacts.get(customId);
    }

    public List<HoldedContact> getAllContacts() {
        return new ArrayList<>(contacts.values());
    }

    @Override
    public HoldedCreationDataInvoice createInvoice(HoldedContact contact, String concept, String description, int amount, double price) {
        var item = new HoldedCreationDataInvoiceItem(concept, description, amount, price);
        return new HoldedCreationDataInvoiceDTOFake(Arrays.asList(item));
    }

    @Override
    public HoldedStatus sendInvoice(HoldedCreationDataInvoice invoice, List<HoldedEmail> emails) {
        var strEmails = HoldedEmail.getRecipients(emails);

        List<HoldedCreationDataInvoice> sentList;
        if (!sentInvoices.containsKey(strEmails)) {
            sentList = new ArrayList<>();
            sentInvoices.put(strEmails, sentList);
        }
        sentList = sentInvoices.get(strEmails);
        sentList.add(invoice);
        return new HoldedStatusDTOFake(
                HoldedStatus.OK,
                "RANDOM_INFO",
                invoice.getId()
        );
    }

    public void reset() {
        this.contacts.clear();
        this.sentInvoices.clear();;
    }

    public List<HoldedCreationDataInvoice> getSentInvoices(String emails) {
        List<HoldedCreationDataInvoice> sentList;
        if (!sentInvoices.containsKey(emails)) {
            return Arrays.asList();
        }
        sentList = sentInvoices.get(emails);
        return sentList;
    }
}
