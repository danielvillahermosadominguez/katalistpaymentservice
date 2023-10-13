package com.codurance.katalyst.payment.application.unit.model;

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
    public static final String CONCEPT = "RANDOM_CONCEPT_NAME";
    public static final String DESCRIPTION = "RANDOM_DESCRIPTION";
    public static final double PRICE = 55.5;
    public static final String NAME = "RANDOM_NAME";
    public static final String SURNAME = "RANDOM_SURNAME";
    public static final String NIF_CIF = "RANDOM_NIF_CIF";
    public static final String COMPANY_NAME = "N/A";
    public static final String PHONE_NUMBER = "RANDOM_PHONE_NUMBER";
    public static final String ADDRESS = "RANDOM_ADDRESS";
    public static final String POSTAL_CODE = "RANDOM_POSTAL_CODE";
    public static final String CITY = "RANDOM_CITY";
    public static final String REGION = "RANDOM_REGION";
    public static final String COUNTRY_CODE = "ES";
    public static final String EMAIL = "RANDOM_EMAIL@EMAIL.COM";
    public static final String ORDER = "RANDOM_ORDER";
    public static final int TRANSACTION_ID = 123456;
    private PurchaseService purchaseService;
    private PurchaseRepository purchaseRepository;
    private Purchase purchaseFixture;

    @BeforeEach
    void beforeEach() {
        purchaseRepository = mock(PurchaseRepository.class);
        purchaseService = new PurchaseService(purchaseRepository);
        purchaseFixture = createPurchaseFixture();
    }

    @Test
    void save_a_purchase() {
        var purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);

        purchaseService.save(purchaseFixture);

        verify(purchaseRepository, times(1)).save(purchaseCaptor.capture());
        var savedPurchase = purchaseCaptor.getValue();
        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(savedPurchase.getCourseId()).isEqualTo("1");
        assertThat(savedPurchase.getConcept()).isEqualTo(CONCEPT);
        assertThat(savedPurchase.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(savedPurchase.getPrice()).isEqualTo(PRICE);
        assertThat(savedPurchase.getName()).isEqualTo(NAME);
        assertThat(savedPurchase.getSurname()).isEqualTo(SURNAME);
        assertThat(savedPurchase.getNifCif()).isEqualTo(NIF_CIF);
        assertThat(savedPurchase.isCompany()).isEqualTo(false);
        assertThat(savedPurchase.getCompany()).isEqualTo(COMPANY_NAME);
        assertThat(savedPurchase.getEmail()).isEqualTo(EMAIL);
        assertThat(savedPurchase.getPhone()).isEqualTo(PHONE_NUMBER);
        assertThat(savedPurchase.getOrder()).isEqualTo(ORDER);
        assertThat(savedPurchase.getTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(savedPurchase.getAddress()).isEqualTo(ADDRESS);
        assertThat(savedPurchase.getPostalCode()).isEqualTo(POSTAL_CODE);
        assertThat(savedPurchase.getCity()).isEqualTo(CITY);
        assertThat(savedPurchase.getRegion()).isEqualTo(REGION);
        assertThat(savedPurchase.getCountry()).isEqualTo(COUNTRY_CODE);
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
        assertThat(savedPurchase.getTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(savedPurchase.getCourseId()).isEqualTo("1");
        assertThat(savedPurchase.getConcept()).isEqualTo(CONCEPT);
        assertThat(savedPurchase.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(savedPurchase.getPrice()).isEqualTo(PRICE);
        assertThat(savedPurchase.getName()).isEqualTo(NAME);
        assertThat(savedPurchase.getSurname()).isEqualTo(SURNAME);
        assertThat(savedPurchase.getNifCif()).isEqualTo(NIF_CIF);
        assertThat(savedPurchase.isCompany()).isFalse();
        assertThat(savedPurchase.getCompany()).isEqualTo(COMPANY_NAME);
        assertThat(savedPurchase.getEmail()).isEqualTo(EMAIL);
        assertThat(savedPurchase.getPhone()).isEqualTo(PHONE_NUMBER);
        assertThat(savedPurchase.getOrder()).isEqualTo(ORDER);
        assertThat(savedPurchase.getTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(savedPurchase.getAddress()).isEqualTo(ADDRESS);
        assertThat(savedPurchase.getPostalCode()).isEqualTo(POSTAL_CODE);
        assertThat(savedPurchase.getCity()).isEqualTo(CITY);
        assertThat(savedPurchase.getRegion()).isEqualTo(REGION);
        assertThat(savedPurchase.getCountry()).isEqualTo(COUNTRY_CODE);
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
        assertThat(savedPurchase.getTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(savedPurchase.getCourseId()).isEqualTo("1");
        assertThat(savedPurchase.getConcept()).isEqualTo(CONCEPT);
        assertThat(savedPurchase.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(savedPurchase.getPrice()).isEqualTo(PRICE);
        assertThat(savedPurchase.getName()).isEqualTo(NAME);
        assertThat(savedPurchase.getSurname()).isEqualTo(SURNAME);
        assertThat(savedPurchase.getNifCif()).isEqualTo(NIF_CIF);
        assertThat(savedPurchase.isCompany()).isFalse();
        assertThat(savedPurchase.getCompany()).isEqualTo(COMPANY_NAME);
        assertThat(savedPurchase.getEmail()).isEqualTo(EMAIL);
        assertThat(savedPurchase.getPhone()).isEqualTo(PHONE_NUMBER);
        assertThat(savedPurchase.getOrder()).isEqualTo(ORDER);
        assertThat(savedPurchase.getTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(savedPurchase.getAddress()).isEqualTo(ADDRESS);
        assertThat(savedPurchase.getPostalCode()).isEqualTo(POSTAL_CODE);
        assertThat(savedPurchase.getCity()).isEqualTo(CITY);
        assertThat(savedPurchase.getRegion()).isEqualTo(REGION);
        assertThat(savedPurchase.getCountry()).isEqualTo(COUNTRY_CODE);
        assertThat(savedPurchase.isProcessedInLearningState()).isTrue();
        assertThat(purchase.isProcessedInFinantialState()).isFalse();
    }

    @Test
    void recover_a_purchase_based_on_transaction_id_and_it_exists() {
        when(purchaseRepository.findPurchaseByTransactionId(TRANSACTION_ID)).thenReturn(purchaseFixture);
        var purchase = purchaseService.getPurchase(TRANSACTION_ID);
        verify(purchaseRepository, times(1)).findPurchaseByTransactionId(TRANSACTION_ID);
        assertThat(purchase).isNotNull();
    }

    @Test
    void recover_a_null_purchase_based_on_transaction_id_and_it_not_exists() {
        var purchase = purchaseService.getPurchase(TRANSACTION_ID);
        verify(purchaseRepository, times(1)).findPurchaseByTransactionId(TRANSACTION_ID);
        assertThat(purchase).isNull();
    }

    private Purchase createPurchaseFixture() {
        return new Purchase(
                TRANSACTION_ID,
                ORDER,
                "1",
                CONCEPT,
                DESCRIPTION,
                PRICE,
                EMAIL,
                NAME,
                SURNAME,
                NIF_CIF,
                false,
                COMPANY_NAME,
                PHONE_NUMBER,
                ADDRESS,
                POSTAL_CODE,
                CITY,
                REGION,
                COUNTRY_CODE,
                false,
                false
        );
    }
}
