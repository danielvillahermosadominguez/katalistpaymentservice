package com.codurance.katalyst.payment.application.fixtures;

import com.codurance.katalyst.payment.application.model.purchase.Purchase;

import static org.assertj.core.api.Assertions.assertThat;

public class PurchaseFixtures {
    public static final int TRANSACTION_ID = 123456;
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

    public Purchase createPurchase() {
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

    public void assertTheSameDataThan(Purchase savedPurchase, Purchase purchase) {
        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getTransactionId()).isEqualTo(purchase.getTransactionId());
        assertThat(savedPurchase.getCourseId()).isEqualTo(purchase.getCourseId());
        assertThat(savedPurchase.getConcept()).isEqualTo(purchase.getConcept());
        assertThat(savedPurchase.getDescription()).isEqualTo(purchase.getDescription());
        assertThat(savedPurchase.getPrice()).isEqualTo(purchase.getPrice());
        assertThat(savedPurchase.getName()).isEqualTo(purchase.getName());
        assertThat(savedPurchase.getSurname()).isEqualTo(purchase.getSurname());
        assertThat(savedPurchase.getNifCif()).isEqualTo(purchase.getNifCif());
        assertThat(savedPurchase.isCompany()).isEqualTo(purchase.isCompany());
        assertThat(savedPurchase.getCompany()).isEqualTo(purchase.getCompany());
        assertThat(savedPurchase.getEmail()).isEqualTo(purchase.getEmail());
        assertThat(savedPurchase.getPhone()).isEqualTo(purchase.getPhone());
        assertThat(savedPurchase.getOrder()).isEqualTo(purchase.getOrder());
        assertThat(savedPurchase.getAddress()).isEqualTo(purchase.getAddress());
        assertThat(savedPurchase.getPostalCode()).isEqualTo(purchase.getPostalCode());
        assertThat(savedPurchase.getCity()).isEqualTo(purchase.getCity());
        assertThat(savedPurchase.getRegion()).isEqualTo(purchase.getRegion());
        assertThat(savedPurchase.getCountry()).isEqualTo(purchase.getCountry());
        assertThat(savedPurchase.isProcessedInLearningState()).isEqualTo(purchase.isProcessedInLearningState());
        assertThat(savedPurchase.isProcessedInFinantialState()).isEqualTo(purchase.isProcessedInFinantialState());
    }

    public void assertTheSameDataThanDefaultPurchase(Purchase savedPurchase) {
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
        assertThat(savedPurchase.isProcessedInFinantialState()).isFalse();
    }
}
