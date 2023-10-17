package com.codurance.katalyst.payment.application.unit.model;

import com.codurance.katalyst.payment.application.fixtures.PurchaseBuilder;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseRepository;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
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
    private PurchaseBuilder fixtures = new PurchaseBuilder();

    @BeforeEach
    void beforeEach() {
        purchaseRepository = mock(PurchaseRepository.class);
        purchaseService = new PurchaseService(purchaseRepository);
        purchaseFixture = fixtures.createPurchaseWithValuesByDefault();
    }

    @Test
    void save_a_purchase() {
        var purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);

        purchaseService.save(purchaseFixture);

        verify(purchaseRepository, times(1)).save(purchaseCaptor.capture());
        var savedPurchase = purchaseCaptor.getValue();
        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseBuilder.TRANSACTION_ID);
        assertThat(savedPurchase.getCourseId()).isEqualTo("1");
        assertThat(savedPurchase.getConcept()).isEqualTo(PurchaseBuilder.CONCEPT);
        assertThat(savedPurchase.getDescription()).isEqualTo(PurchaseBuilder.DESCRIPTION);
        assertThat(savedPurchase.getPrice()).isEqualTo(PurchaseBuilder.PRICE);
        assertThat(savedPurchase.getName()).isEqualTo(PurchaseBuilder.NAME);
        assertThat(savedPurchase.getSurname()).isEqualTo(PurchaseBuilder.SURNAME);
        assertThat(savedPurchase.getNifCif()).isEqualTo(PurchaseBuilder.NIF_CIF);
        assertThat(savedPurchase.isCompany()).isEqualTo(false);
        assertThat(savedPurchase.getCompany()).isEqualTo(PurchaseBuilder.COMPANY_NAME);
        assertThat(savedPurchase.getEmail()).isEqualTo(PurchaseBuilder.EMAIL);
        assertThat(savedPurchase.getPhone()).isEqualTo(PurchaseBuilder.PHONE_NUMBER);
        assertThat(savedPurchase.getOrder()).isEqualTo(PurchaseBuilder.ORDER);
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseBuilder.TRANSACTION_ID);
        assertThat(savedPurchase.getAddress()).isEqualTo(PurchaseBuilder.ADDRESS);
        assertThat(savedPurchase.getPostalCode()).isEqualTo(PurchaseBuilder.POSTAL_CODE);
        assertThat(savedPurchase.getCity()).isEqualTo(PurchaseBuilder.CITY);
        assertThat(savedPurchase.getRegion()).isEqualTo(PurchaseBuilder.REGION);
        assertThat(savedPurchase.getCountry()).isEqualTo(PurchaseBuilder.COUNTRY_CODE);
        assertThat(savedPurchase.isProcessedInLearningState()).isEqualTo(false);
        assertThat(savedPurchase.isCompany()).isEqualTo(false);
        assertThat(savedPurchase.isProcessedInFinantialState()).isEqualTo(false);
    }

    @Test
    void update_financial_step_to_true_when_it_is_overcome() {
        var purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        when(purchaseRepository.save(any())).thenReturn(purchaseFixture);
        when(purchaseRepository.findPurchaseById(purchaseFixture.getId())).thenReturn(purchaseFixture);

        var purchase = purchaseService.updateFinantialStepFor(purchaseFixture, true);

        verify(purchaseRepository, times(1)).save(purchaseCaptor.capture());
        var savedPurchase = purchaseCaptor.getValue();
        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseBuilder.TRANSACTION_ID);
        assertThat(savedPurchase.getCourseId()).isEqualTo("1");
        assertThat(savedPurchase.getConcept()).isEqualTo(PurchaseBuilder.CONCEPT);
        assertThat(savedPurchase.getDescription()).isEqualTo(PurchaseBuilder.DESCRIPTION);
        assertThat(savedPurchase.getPrice()).isEqualTo(PurchaseBuilder.PRICE);
        assertThat(savedPurchase.getName()).isEqualTo(PurchaseBuilder.NAME);
        assertThat(savedPurchase.getSurname()).isEqualTo(PurchaseBuilder.SURNAME);
        assertThat(savedPurchase.getNifCif()).isEqualTo(PurchaseBuilder.NIF_CIF);
        assertThat(savedPurchase.isCompany()).isFalse();
        assertThat(savedPurchase.getCompany()).isEqualTo(PurchaseBuilder.COMPANY_NAME);
        assertThat(savedPurchase.getEmail()).isEqualTo(PurchaseBuilder.EMAIL);
        assertThat(savedPurchase.getPhone()).isEqualTo(PurchaseBuilder.PHONE_NUMBER);
        assertThat(savedPurchase.getOrder()).isEqualTo(PurchaseBuilder.ORDER);
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseBuilder.TRANSACTION_ID);
        assertThat(savedPurchase.getAddress()).isEqualTo(PurchaseBuilder.ADDRESS);
        assertThat(savedPurchase.getPostalCode()).isEqualTo(PurchaseBuilder.POSTAL_CODE);
        assertThat(savedPurchase.getCity()).isEqualTo(PurchaseBuilder.CITY);
        assertThat(savedPurchase.getRegion()).isEqualTo(PurchaseBuilder.REGION);
        assertThat(savedPurchase.getCountry()).isEqualTo(PurchaseBuilder.COUNTRY_CODE);
        assertThat(savedPurchase.isProcessedInLearningState()).isFalse();
        assertThat(purchase.isProcessedInFinantialState()).isTrue();
        assertThat(savedPurchase.isProcessedInFinantialState()).isTrue();
    }

    @Test
    void update_learning_state() {
        var purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        when(purchaseRepository.save(any())).thenReturn(purchaseFixture);
        when(purchaseRepository.save(any())).thenReturn(purchaseFixture);
        when(purchaseRepository.findPurchaseById(purchaseFixture.getId())).thenReturn(purchaseFixture);

        var purchase = purchaseService.updateLearningStepFor(purchaseFixture, true);

        verify(purchaseRepository, times(1)).save(purchaseCaptor.capture());

        var savedPurchase = purchaseCaptor.getValue();
        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseBuilder.TRANSACTION_ID);
        assertThat(savedPurchase.getCourseId()).isEqualTo("1");
        assertThat(savedPurchase.getConcept()).isEqualTo(PurchaseBuilder.CONCEPT);
        assertThat(savedPurchase.getDescription()).isEqualTo(PurchaseBuilder.DESCRIPTION);
        assertThat(savedPurchase.getPrice()).isEqualTo(PurchaseBuilder.PRICE);
        assertThat(savedPurchase.getName()).isEqualTo(PurchaseBuilder.NAME);
        assertThat(savedPurchase.getSurname()).isEqualTo(PurchaseBuilder.SURNAME);
        assertThat(savedPurchase.getNifCif()).isEqualTo(PurchaseBuilder.NIF_CIF);
        assertThat(savedPurchase.isCompany()).isFalse();
        assertThat(savedPurchase.getCompany()).isEqualTo(PurchaseBuilder.COMPANY_NAME);
        assertThat(savedPurchase.getEmail()).isEqualTo(PurchaseBuilder.EMAIL);
        assertThat(savedPurchase.getPhone()).isEqualTo(PurchaseBuilder.PHONE_NUMBER);
        assertThat(savedPurchase.getOrder()).isEqualTo(PurchaseBuilder.ORDER);
        assertThat(savedPurchase.getTransactionId()).isEqualTo(PurchaseBuilder.TRANSACTION_ID);
        assertThat(savedPurchase.getAddress()).isEqualTo(PurchaseBuilder.ADDRESS);
        assertThat(savedPurchase.getPostalCode()).isEqualTo(PurchaseBuilder.POSTAL_CODE);
        assertThat(savedPurchase.getCity()).isEqualTo(PurchaseBuilder.CITY);
        assertThat(savedPurchase.getRegion()).isEqualTo(PurchaseBuilder.REGION);
        assertThat(savedPurchase.getCountry()).isEqualTo(PurchaseBuilder.COUNTRY_CODE);
        assertThat(savedPurchase.isProcessedInLearningState()).isTrue();
        assertThat(purchase.isProcessedInFinantialState()).isFalse();
    }

    @Test
    void recover_a_purchase_based_on_transaction_id_and_it_exists() {
        when(purchaseRepository.findPurchaseByTransactionId(PurchaseBuilder.TRANSACTION_ID)).thenReturn(purchaseFixture);
        var purchase = purchaseService.getPurchase(PurchaseBuilder.TRANSACTION_ID);
        verify(purchaseRepository, times(1)).findPurchaseByTransactionId(PurchaseBuilder.TRANSACTION_ID);
        assertThat(purchase).isNotNull();
    }

    @Test
    void recover_a_null_purchase_based_on_transaction_id_and_it_not_exists() {
        var purchase = purchaseService.getPurchase(PurchaseBuilder.TRANSACTION_ID);
        verify(purchaseRepository, times(1)).findPurchaseByTransactionId(PurchaseBuilder.TRANSACTION_ID);
        assertThat(purchase).isNull();
    }
}
