package com.codurance.katalyst.payment.application.builders;

import com.codurance.katalyst.payment.application.model.customer.CustomerData;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PurchaseBuilder {
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

    Purchase item;

    public PurchaseBuilder create(Map<String, String> purchaseMap) {
        var order = purchaseMap.get("ORDER CODE");
        var courseId = purchaseMap.get("COURSE ID");
        var concept = purchaseMap.get("CONCEPT");
        var description = purchaseMap.get("DESCRIPTION");
        var price = Double.parseDouble(purchaseMap.get("PRICE"));
        var email = purchaseMap.get("EMAIL");
        var name = purchaseMap.get("FIRST NAME");
        var surname = purchaseMap.get("SURNAME");
        var nifCif = purchaseMap.get("NIF/CIF");
        var isCompany = purchaseMap.get("IS COMPANY").equals("YES");
        var company = purchaseMap.get("COMPANY NAME");
        var phone = purchaseMap.get("PHONE NUMBER");
        var address = purchaseMap.get("ADDRESS");
        var postalCode = purchaseMap.get("POSTAL CODE");
        var city = purchaseMap.get("CITY");
        var region = purchaseMap.get("REGION");
        var country = purchaseMap.get("COUNTRY");
        var finantialStepOvercome = false;
        var learningStepOvercome = false;

        item = new Purchase(
                0,
                order,
                courseId,
                concept,
                description,
                price,
                email,
                name,
                surname,
                nifCif,
                isCompany,
                company,
                phone,
                address,
                postalCode,
                city,
                region,
                country,
                finantialStepOvercome,
                learningStepOvercome
        );
        return this;
    }

    public Purchase getItem() {
        return item;
    }

    public PurchaseBuilder createWithDefaultValues() {
        item = createPurchaseWithValuesByDefault();
        return this;
    }

    public PurchaseBuilder createFromCustomerData(CustomerData customerData) {
        item = createPurchaseFromCustomData(customerData);
        return this;
    }

    private Purchase createPurchaseWithValuesByDefault() {
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

    public PurchaseBuilder transactionId(int transactionId) {
        this.item.setTransactionId(transactionId);
        return this;
    }

    public PurchaseBuilder financialStepOvercome(boolean financialStepOvercome) {
        this.item.setFinantialStepOvercome(financialStepOvercome);
        return this;
    }

    public PurchaseBuilder learningStepOvercome(boolean learningStepOvercome) {
        this.item.setLearningStepOvercome(learningStepOvercome);
        return this;
    }

    private Purchase createPurchaseFromCustomData(CustomerData customerData) {
        return new Purchase(
                0,
                "",
                "0",
                "",
                "",
                0,
                customerData.getEmail(),
                customerData.getName(),
                customerData.getSurname(),
                customerData.getDnicif(),
                customerData.getIsCompany(),
                customerData.getCompany(),
                customerData.getPhoneNumber(),
                customerData.getAddress(),
                customerData.getPostalCode(),
                customerData.getCity(),
                customerData.getRegion(),
                customerData.getCountry(),
                false,
                false
        );
    }

    public PurchaseBuilder order(String order) {
        item.setOrder(order);
        return this;
    }

    public PurchaseBuilder courseId(String courseId) {
        item.setCourseId(courseId);
        return this;
    }

    public PurchaseBuilder concept(String value) {
        item.setConcept(value);
        return this;
    }

    public PurchaseBuilder description(String value) {
        item.setDescription(value);
        return this;
    }

    public PurchaseBuilder price(double value) {
        item.setPrice(value);
        return this;
    }
}
