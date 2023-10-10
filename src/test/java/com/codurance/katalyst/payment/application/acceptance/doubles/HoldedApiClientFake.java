package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.holded.requests.CreateInvoiceItemRequestBody;
import com.codurance.katalyst.payment.application.ports.holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedInvoiceInfo;
import com.codurance.katalyst.payment.application.ports.holded.dto.HoldedStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoldedApiClientFake implements HoldedApiClient {

    public static int holdedCounterId = 0;

    class HoldedCreationDataInvoiceDTOFake extends HoldedInvoiceInfo {
        public static int idCounter = 0;

        protected List<CreateInvoiceItemRequestBody> items;

        public List<CreateInvoiceItemRequestBody> getItems() {
            return this.items;
        }

        public HoldedCreationDataInvoiceDTOFake(List<CreateInvoiceItemRequestBody> items) {
            super();
            this.id = ++idCounter + "";
            this.items = items;
        }
    }

    private Map<String, HoldedContact> contacts = new HashMap<>();

    private Map<String, List<HoldedInvoiceInfo>> sentInvoices = new HashMap<>();

    @Override
    public HoldedContact createContact(HoldedContact contact) {
        contact.setId(++holdedCounterId + "");
        contacts.put(contact.getCustomId(), contact);
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
    public HoldedInvoiceInfo createInvoice(HoldedContact contact, String concept, String description, int amount, double price) {
        var item = new CreateInvoiceItemRequestBody(concept, description, amount, price);
        return new HoldedCreationDataInvoiceDTOFake(Arrays.asList(item));
    }

    @Override
    public HoldedStatus sendInvoice(HoldedInvoiceInfo invoice, List<HoldedEmail> emails) {
        var strEmails = HoldedEmail.getRecipients(emails);

        List<HoldedInvoiceInfo> sentList;
        if (!sentInvoices.containsKey(strEmails)) {
            sentList = new ArrayList<>();
            sentInvoices.put(strEmails, sentList);
        }
        sentList = sentInvoices.get(strEmails);
        sentList.add(invoice);
        return new HoldedStatus(
                HoldedStatus.OK,
                "RANDOM_INFO",
                invoice.getId()
        );
    }

    public void reset() {
        this.contacts.clear();
        this.sentInvoices.clear();
    }

    public List<HoldedInvoiceInfo> getSentInvoices(String emails) {
        if (!sentInvoices.containsKey(emails)) {
            return Arrays.asList();
        }
        return sentInvoices.get(emails);
    }

    public List<CreateInvoiceItemRequestBody> getSentItemsInTheResponseFor(HoldedInvoiceInfo invoice) {
        var fakeInvoice = (HoldedCreationDataInvoiceDTOFake) invoice;
        return fakeInvoice.items;
    }
}
