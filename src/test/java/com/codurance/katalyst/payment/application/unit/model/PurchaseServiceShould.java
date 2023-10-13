package com.codurance.katalyst.payment.application.unit.model;

import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseRepository;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import com.codurance.katalyst.payment.application.unit.model.fixtures.PurchaseFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PurchaseServiceShould {
    private PurchaseService purchaseService;
    private PurchaseRepository purchaseRepository;
    private Purchase purchaseFixture;
    private PurchaseFixtures fixtures = new PurchaseFixtures();

    @BeforeEach
    void beforeEach() {
        purchaseRepository = mock(PurchaseRepository.class);
        purchaseService = new PurchaseService(purchaseRepository);
        purchaseFixture = fixtures.createPurchase();
    }

    @Test
    void save_a_purchase() {
        var purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);

        purchaseService.save(purchaseFixture);

        verify(purchaseRepository, times(1)).save(purchaseCaptor.capture());
        var savedPurchase = purchaseCaptor.getValue();
        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseFixtures.TRANSACTION_ID);
        assertThat(savedPurchase.getCourseId()).isEqualTo("1");
        assertThat(savedPurchase.getConcept()).isEqualTo(PurchaseFixtures.CONCEPT);
        assertThat(savedPurchase.getDescription()).isEqualTo(PurchaseFixtures.DESCRIPTION);
        assertThat(savedPurchase.getPrice()).isEqualTo(PurchaseFixtures.PRICE);
        assertThat(savedPurchase.getName()).isEqualTo(PurchaseFixtures.NAME);
        assertThat(savedPurchase.getSurname()).isEqualTo(PurchaseFixtures.SURNAME);
        assertThat(savedPurchase.getNifCif()).isEqualTo(PurchaseFixtures.NIF_CIF);
        assertThat(savedPurchase.isCompany()).isEqualTo(false);
        assertThat(savedPurchase.getCompany()).isEqualTo(PurchaseFixtures.COMPANY_NAME);
        assertThat(savedPurchase.getEmail()).isEqualTo(PurchaseFixtures.EMAIL);
        assertThat(savedPurchase.getPhone()).isEqualTo(PurchaseFixtures.PHONE_NUMBER);
        assertThat(savedPurchase.getOrder()).isEqualTo(PurchaseFixtures.ORDER);
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseFixtures.TRANSACTION_ID);
        assertThat(savedPurchase.getAddress()).isEqualTo(PurchaseFixtures.ADDRESS);
        assertThat(savedPurchase.getPostalCode()).isEqualTo(PurchaseFixtures.POSTAL_CODE);
        assertThat(savedPurchase.getCity()).isEqualTo(PurchaseFixtures.CITY);
        assertThat(savedPurchase.getRegion()).isEqualTo(PurchaseFixtures.REGION);
        assertThat(savedPurchase.getCountry()).isEqualTo(PurchaseFixtures.COUNTRY_CODE);
        assertThat(savedPurchase.isProcessedInLearningState()).isEqualTo(false);
        assertThat(savedPurchase.isCompany()).isEqualTo(false);
        assertThat(savedPurchase.isProcessedInFinantialState()).isEqualTo(false);
    }

    @Test
    void update_finantial_state() {
        var purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        when(purchaseRepository.update(any())).thenReturn(purchaseFixture);
        when(purchaseRepository.findPurchaseById(purchaseFixture.getId())).thenReturn(purchaseFixture);

        var purchase = purchaseService.updateFinantialStepFor(purchaseFixture, true);

        verify(purchaseRepository, times(1)).update(purchaseCaptor.capture());
        var savedPurchase = purchaseCaptor.getValue();
        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseFixtures.TRANSACTION_ID);
        assertThat(savedPurchase.getCourseId()).isEqualTo("1");
        assertThat(savedPurchase.getConcept()).isEqualTo(PurchaseFixtures.CONCEPT);
        assertThat(savedPurchase.getDescription()).isEqualTo(PurchaseFixtures.DESCRIPTION);
        assertThat(savedPurchase.getPrice()).isEqualTo(PurchaseFixtures.PRICE);
        assertThat(savedPurchase.getName()).isEqualTo(PurchaseFixtures.NAME);
        assertThat(savedPurchase.getSurname()).isEqualTo(PurchaseFixtures.SURNAME);
        assertThat(savedPurchase.getNifCif()).isEqualTo(PurchaseFixtures.NIF_CIF);
        assertThat(savedPurchase.isCompany()).isFalse();
        assertThat(savedPurchase.getCompany()).isEqualTo(PurchaseFixtures.COMPANY_NAME);
        assertThat(savedPurchase.getEmail()).isEqualTo(PurchaseFixtures.EMAIL);
        assertThat(savedPurchase.getPhone()).isEqualTo(PurchaseFixtures.PHONE_NUMBER);
        assertThat(savedPurchase.getOrder()).isEqualTo(PurchaseFixtures.ORDER);
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseFixtures.TRANSACTION_ID);
        assertThat(savedPurchase.getAddress()).isEqualTo(PurchaseFixtures.ADDRESS);
        assertThat(savedPurchase.getPostalCode()).isEqualTo(PurchaseFixtures.POSTAL_CODE);
        assertThat(savedPurchase.getCity()).isEqualTo(PurchaseFixtures.CITY);
        assertThat(savedPurchase.getRegion()).isEqualTo(PurchaseFixtures.REGION);
        assertThat(savedPurchase.getCountry()).isEqualTo(PurchaseFixtures.COUNTRY_CODE);
        assertThat(savedPurchase.isProcessedInLearningState()).isFalse();
        assertThat(purchase.isProcessedInFinantialState()).isTrue();
        assertThat(savedPurchase.isProcessedInFinantialState()).isTrue();
    }

    @Test
    void update_learning_state() {
        var purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        when(purchaseRepository.update(any())).thenReturn(purchaseFixture);
        when(purchaseRepository.update(any())).thenReturn(purchaseFixture);
        when(purchaseRepository.findPurchaseById(purchaseFixture.getId())).thenReturn(purchaseFixture);

        var purchase = purchaseService.updateLearningStepFor(purchaseFixture, true);

        verify(purchaseRepository, times(1)).update(purchaseCaptor.capture());

        var savedPurchase = purchaseCaptor.getValue();
        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseFixtures.TRANSACTION_ID);
        assertThat(savedPurchase.getCourseId()).isEqualTo("1");
        assertThat(savedPurchase.getConcept()).isEqualTo(PurchaseFixtures.CONCEPT);
        assertThat(savedPurchase.getDescription()).isEqualTo(PurchaseFixtures.DESCRIPTION);
        assertThat(savedPurchase.getPrice()).isEqualTo(PurchaseFixtures.PRICE);
        assertThat(savedPurchase.getName()).isEqualTo(PurchaseFixtures.NAME);
        assertThat(savedPurchase.getSurname()).isEqualTo(PurchaseFixtures.SURNAME);
        assertThat(savedPurchase.getNifCif()).isEqualTo(PurchaseFixtures.NIF_CIF);
        assertThat(savedPurchase.isCompany()).isFalse();
        assertThat(savedPurchase.getCompany()).isEqualTo(PurchaseFixtures.COMPANY_NAME);
        assertThat(savedPurchase.getEmail()).isEqualTo(PurchaseFixtures.EMAIL);
        assertThat(savedPurchase.getPhone()).isEqualTo(PurchaseFixtures.PHONE_NUMBER);
        assertThat(savedPurchase.getOrder()).isEqualTo(PurchaseFixtures.ORDER);
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseFixtures.TRANSACTION_ID);
        assertThat(savedPurchase.getAddress()).isEqualTo(PurchaseFixtures.ADDRESS);
        assertThat(savedPurchase.getPostalCode()).isEqualTo(PurchaseFixtures.POSTAL_CODE);
        assertThat(savedPurchase.getCity()).isEqualTo(PurchaseFixtures.CITY);
        assertThat(savedPurchase.getRegion()).isEqualTo(PurchaseFixtures.REGION);
        assertThat(savedPurchase.getCountry()).isEqualTo(PurchaseFixtures.COUNTRY_CODE);
        assertThat(savedPurchase.isProcessedInLearningState()).isTrue();
        assertThat(purchase.isProcessedInFinantialState()).isFalse();
    }

    @Test
    void recover_a_purchase_based_on_transaction_id_and_it_exists() {
        when(purchaseRepository.findPurchaseByTransactionId(PurchaseFixtures.TRANSACTION_ID)).thenReturn(purchaseFixture);
        var purchase = purchaseService.getPurchase(PurchaseFixtures.TRANSACTION_ID);
        verify(purchaseRepository, times(1)).findPurchaseByTransactionId(PurchaseFixtures.TRANSACTION_ID);
        assertThat(purchase).isNotNull();
    }

    @Test
    void recover_a_null_purchase_based_on_transaction_id_and_it_not_exists() {
        var purchase = purchaseService.getPurchase(PurchaseFixtures.TRANSACTION_ID);
        verify(purchaseRepository, times(1)).findPurchaseByTransactionId(PurchaseFixtures.TRANSACTION_ID);
        assertThat(purchase).isNull();
    }

}
