package com.codurance.katalyst.payment.application.model.financial;

import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.model.ports.holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedBillAddress;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedTypeContact;
import com.codurance.katalyst.payment.application.model.ports.holded.exceptions.HoldedNotRespond;
import com.codurance.katalyst.payment.application.model.ports.holded.exceptions.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@Service
public class FinancialService {

    private HoldedApiClient holdedApiClient;

    @Autowired
    public FinancialService(HoldedApiClient holdedApiClient) {

        this.holdedApiClient = holdedApiClient;
    }

    public boolean emitInvoice(Purchase purchase) throws InvalidInputCustomerData, FinancialPlatformIsNotAvailable {
        try {
            createContactAndInvoicing(purchase);
        } catch (HoldedNotRespond exception) {
            throw new FinancialPlatformIsNotAvailable();
        } catch (NotValidEMailFormat | UnsupportedEncodingException exception) {
            throw new InvalidInputCustomerData(exception.getMessage());
        }
        return true;
    }

    private void createContactAndInvoicing(Purchase purchase) throws NotValidEMailFormat, UnsupportedEncodingException, HoldedNotRespond {
        var email = new HoldedEmail(purchase.getEmail());

        var customId = HoldedContact.buildCustomId(purchase.getNifCif(), email);
        var contact = holdedApiClient.getContactByCustomId(customId);
        if (contact == null) {
            contact = this.createContactInHolded(purchase);
        }
        var concept = purchase.getConcept();
        var description = purchase.getDescription();
        var amount = 1;
        var price = purchase.getPrice();
        var invoice = holdedApiClient.createInvoice(contact,
                concept.toUpperCase(),
                description.toUpperCase(),
                amount,
                price);
        holdedApiClient.sendInvoice(invoice, Arrays.asList(contact.getEmail()));
    }

    private HoldedContact createContactInHolded(Purchase originalDataPurchase) throws NotValidEMailFormat, HoldedNotRespond {
        var name = originalDataPurchase.getCompany().toUpperCase();
        var type = HoldedTypeContact.CLIENT;
        var isPerson = !originalDataPurchase.isCompany();
        String code = null;
        var vatNumber = originalDataPurchase.getNifCif().toUpperCase();
        if (!originalDataPurchase.isCompany()) {
            code = vatNumber;
            vatNumber = null;
            name = originalDataPurchase.getName().toUpperCase() + " " + originalDataPurchase.getSurname().toUpperCase();
        }
        var billingAddress = new HoldedBillAddress(
                originalDataPurchase.getAddress().toUpperCase(),
                originalDataPurchase.getPostalCode().toUpperCase(),
                originalDataPurchase.getCity().toUpperCase(),
                originalDataPurchase.getRegion().toUpperCase(),
                originalDataPurchase.getCountry().toUpperCase());
        var contact = new HoldedContact(
                name,
                code,
                vatNumber,
                type,
                isPerson,
                new HoldedEmail(originalDataPurchase.getEmail()),
                originalDataPurchase.getPhone().toUpperCase(),
                billingAddress,
                "70500000"
        );

        return holdedApiClient.createContact(contact);
    }
}
