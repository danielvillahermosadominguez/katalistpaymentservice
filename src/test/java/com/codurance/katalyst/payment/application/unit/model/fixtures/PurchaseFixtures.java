package com.codurance.katalyst.payment.application.unit.model.fixtures;

import com.codurance.katalyst.payment.application.model.purchase.Purchase;

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
}
