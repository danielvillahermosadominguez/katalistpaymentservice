package com.codurance.katalyst.payment.application.unit.model;

import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.builders.HoldedContactBuilder;
import com.codurance.katalyst.payment.application.builders.PurchaseBuilder;
import com.codurance.katalyst.payment.application.model.financial.FinancialService;
import com.codurance.katalyst.payment.application.model.ports.holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.model.ports.email.Email;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedInvoiceInfo;
import com.codurance.katalyst.payment.application.model.ports.holded.dto.HoldedStatus;
import com.codurance.katalyst.payment.application.model.ports.holded.exceptions.HoldedNotRespond;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FinantialServiceShould {
    private HoldedApiClient holdedApiClient;
    private FinancialService financialService;
    private Purchase purchase;

    @BeforeEach
    void beforeEach() {
        holdedApiClient = mock(HoldedApiClient.class);
        financialService = new FinancialService(holdedApiClient);
        var puchaseBuilder = new PurchaseBuilder();
        purchase = puchaseBuilder
                .createWithDefaultValues()
                .getItem();
    }

    @Test
    void throw_an_exception_if_the_email_is_not_valid() {
        purchase.setEmail("RANDOM_NO_VALID_EMAIL");

        var thrown = assertThrows(InvalidInputCustomerData.class, () -> {
            financialService.emitInvoice(purchase);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void throw_an_exception_if_holded_not_respond() throws HoldedNotRespond {
        when(holdedApiClient.getContactByCustomId(any())).thenThrow(HoldedNotRespond.class);

        var thrown = assertThrows(FinancialPlatformIsNotAvailable.class, () -> {
            financialService.emitInvoice(purchase);
        });

        assertThat(thrown).isNotNull();
    }

    @Test
    void not_to_create_a_contact_if_the_contact_exists_in_holded() throws HoldedNotRespond, FinancialPlatformIsNotAvailable, InvalidInputCustomerData {
        var email = new Email(purchase.getEmail());
        var nifCif = purchase.getNifCif();
        var contact = createBasicContact(email, nifCif);
        var expectedCustomId = contact.getCustomId();
        purchase.setEmail(email.getValue());
        purchase.setNifCif(nifCif);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(contact);

        financialService.emitInvoice(purchase);

        verify(holdedApiClient).getContactByCustomId(expectedCustomId);
        verify(holdedApiClient, never()).createContact(any());
    }

    @Test
    void create_a_company_contact_with_upper_case_data_when_contact_not_exists() throws HoldedNotRespond, FinancialPlatformIsNotAvailable, InvalidInputCustomerData {
        var contactArg = ArgumentCaptor.forClass(HoldedContact.class);
        var companyName = purchase.getCompany();
        var nifCif = purchase.getNifCif();
        var address = purchase.getAddress();
        var postalCode = purchase.getPostalCode();
        var region = purchase.getRegion();
        var city = purchase.getCity();
        var country = purchase.getCountry();
        var email = new Email(purchase.getEmail());
        var contact = createBasicContact(email, nifCif);
        var expectedCustomId = contact.getCustomId();
        purchase.setIsCompany(true);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(holdedApiClient.createContact(any())).thenReturn(contact);

        financialService.emitInvoice(purchase);

        verify(holdedApiClient).getContactByCustomId(expectedCustomId);
        verify(holdedApiClient).createContact(contactArg.capture());
        contact = contactArg.getValue();
        assertThat(contact.getName()).isEqualTo(companyName.toUpperCase());
        assertThat(contact.getCode()).isNull();
        assertThat(contact.getVatNumber()).isEqualTo(nifCif.toUpperCase());
        assertThat(contact.getBillAddress()).isNotNull();
        var billAddress = contact.getBillAddress();
        assertThat(billAddress.getAddress()).isEqualTo(address.toUpperCase());
        assertThat(billAddress.getPostalCode()).isEqualTo(postalCode.toUpperCase());
        assertThat(billAddress.getCity()).isEqualTo(city.toUpperCase());
        assertThat(billAddress.getProvince()).isEqualTo(region.toUpperCase());
        assertThat(billAddress.getCountryCode()).isEqualTo(country);
    }

    @Test
    void create_a_person_contact_with_upper_case_data_when_contact_not_exists() throws HoldedNotRespond, FinancialPlatformIsNotAvailable, InvalidInputCustomerData {
        var contactArg = ArgumentCaptor.forClass(HoldedContact.class);
        var name = purchase.getName();
        var surname = purchase.getSurname();
        var nifCif = purchase.getNifCif();
        var address = purchase.getAddress();
        var postalCode = purchase.getPostalCode();
        var region = purchase.getRegion();
        var city = purchase.getCity();
        var country = purchase.getCountry();
        var email = new Email(purchase.getEmail());
        var contact = createBasicContact(email, nifCif);
        var expectedCustomId = contact.getCustomId();
        purchase.setIsCompany(false);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(holdedApiClient.createContact(any())).thenReturn(contact);

        financialService.emitInvoice(purchase);

        verify(holdedApiClient).getContactByCustomId(expectedCustomId);
        verify(holdedApiClient).createContact(contactArg.capture());
        contact = contactArg.getValue();
        assertThat(contact.getName()).isEqualTo(name.toUpperCase() + " " + surname.toUpperCase());
        assertThat(contact.getCode()).isEqualTo(nifCif.toUpperCase());
        assertThat(contact.getVatNumber()).isNull();
        assertThat(contact.getBillAddress()).isNotNull();
        var billAddress = contact.getBillAddress();
        assertThat(billAddress.getAddress()).isEqualTo(address.toUpperCase());
        assertThat(billAddress.getPostalCode()).isEqualTo(postalCode.toUpperCase());
        assertThat(billAddress.getCity()).isEqualTo(city.toUpperCase());
        assertThat(billAddress.getProvince()).isEqualTo(region.toUpperCase());
        assertThat(billAddress.getCountryCode()).isEqualTo(country);
    }

    @Test
    void not_create_a_contact_when_contact_exists() throws HoldedNotRespond, FinancialPlatformIsNotAvailable, InvalidInputCustomerData {
        var nifCif = purchase.getNifCif();
        var email = new Email(purchase.getEmail());
        var contact = createBasicContact(email, nifCif);
        var expectedCustomId = contact.getCustomId();

        when(holdedApiClient.getContactByCustomId(expectedCustomId)).thenReturn(contact);

        financialService.emitInvoice(purchase);

        verify(holdedApiClient).getContactByCustomId(any());
        verify(holdedApiClient, never()).createContact(any());
    }

    @Test
    void create_an_invoice_with_the_course_data() throws HoldedNotRespond, FinancialPlatformIsNotAvailable, InvalidInputCustomerData {
        var concept = purchase.getConcept().toUpperCase();
        var description = purchase.getDescription().toUpperCase();
        var price = purchase.getPrice();
        var email = purchase.getEmail();
        var nifCif = purchase.getNifCif();
        var contact = createBasicContact(
                new Email(email),
                nifCif
        );
        var invoice = new HoldedInvoiceInfo();

        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(holdedApiClient.createContact(any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);

        financialService.emitInvoice(purchase);

        verify(holdedApiClient).createInvoice(
                any(),
                eq(concept),
                eq(description),
                eq(1),
                eq(price)
        );
    }

    @Test
    void send_the_invoice_to_the_customer_email() throws HoldedNotRespond, FinancialPlatformIsNotAvailable, InvalidInputCustomerData {
        var email = purchase.getEmail();
        var nifCif = purchase.getNifCif();
        var contact = createBasicContact(
                new Email(email),
                nifCif
        );
        var invoice = new HoldedInvoiceInfo();

        ArgumentCaptor<List<Email>> captor = ArgumentCaptor.forClass(List.class);
        when(holdedApiClient.getContactByCustomId(any())).thenReturn(null);
        when(holdedApiClient.createContact(any())).thenReturn(contact);
        when(holdedApiClient.createInvoice(any(), any(), any(), anyInt(), anyDouble())).thenReturn(invoice);
        when(holdedApiClient.sendInvoice(any(), captor.capture())).thenReturn(new HoldedStatus());

        financialService.emitInvoice(purchase);

        verify(holdedApiClient).sendInvoice(
                any(),
                any()
        );

        var emails = captor.getValue();
        assertThat(emails.size()).isEqualTo(1);
        assertThat(emails.get(0).getValue()).isEqualTo(email);
    }

    private HoldedContact createBasicContact(Email email, String nifCif) {
        var holdedContactBuilder = new HoldedContactBuilder();
        return holdedContactBuilder
                .createContactDefault(email, nifCif)
                .getItem();
    }

}
