package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoiceItem;
import com.codurance.katalyst.payment.application.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.holded.dto.HoldedCreationDataInvoice;
import com.codurance.katalyst.payment.application.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.ports.HoldedApiClient;
import com.codurance.katalyst.payment.application.holded.dto.NotValidEMailFormat;

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

    class HoldedCreationDataInvoiceDTOFake extends HoldedCreationDataInvoice {
        public static int idCounter = 0;

        public HoldedCreationDataInvoiceDTOFake(List<HoldedCreationDataInvoiceItem> items) {
            super();
            this.id = ++idCounter + "";
            this.items = new ArrayList<>();
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
    public HoldedContact createContact(String name, String surname, HoldedEmail email, String company, String nifCif) throws UnsupportedEncodingException, NotValidEMailFormat {
        String customId = createCustomId(nifCif, email);
        HoldedContact holdedContact = new HoldedContactDTOFake(customId, name, surname, email.getValue(), company, nifCif);
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
    public HoldedCreationDataInvoice createInvoice(HoldedContact contact, String concept, String description, int amount, double price) {
        var item = new HoldedCreationDataInvoiceItem(concept,description,amount,price);
        return new HoldedCreationDataInvoiceDTOFake(Arrays.asList(item));
    }

    @Override
    public HoldedStatus sendInvoice(HoldedCreationDataInvoice invoice, List<HoldedEmail> emails) {
        String strEmails = HoldedEmail.getRecipients(emails);

        List<HoldedCreationDataInvoice> sentList;
        if (!sentInvoices.containsKey(strEmails)) {
            sentList = new ArrayList<>();
            sentInvoices.put(strEmails, sentList);
        }
        sentList = sentInvoices.get(strEmails);
        sentList.add(invoice);
        return new HoldedStatusDTOFake(HoldedStatus.OK, "RANDOM_INFO", invoice.getId());
    }

    @Override
    public String createCustomId(String nifCif, HoldedEmail email) throws UnsupportedEncodingException, NotValidEMailFormat {
        String customId = nifCif + email.getInUnicodeFormat();
        return customId;
    }

    public void reset() {
        this.contacts.clear();
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
