package com.codurance.katalyst.payment.application.infrastructure.database.purchase;

import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "PURCHASE")
public class DBPurchase {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "transaction_id")
    private int transactionId;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "description")
    private String description;

    @Column(name = "concept")
    private String concept;

    @Column(name = "phone")
    private String phone;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "address")
    private String address;

    @Column(name = "course_id")
    private String courseId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "nif_cif")
    private String nifCif;

    @Column(name = "company")
    private String company;

    @Column(name = "is_company")
    private boolean isCompany;

    @Column(name = "city")
    private String city;

    @Column(name = "region")
    private String region;

    @Column(name = "country")
    private String country;

    @Column(name = "finantial_step_overcome")
    private boolean finantialStepOvercome;

    @Column(name = "learning_step_overcome")
    private boolean learningStepOvercome;

    public DBPurchase(Purchase purchase) {
        this.id = purchase.getId();
        this.transactionId = purchase.getTransactionId();
        this.price = BigDecimal.valueOf(purchase.getPrice());
        this.description = purchase.getDescription();
        this.concept = purchase.getConcept();
        this.phone = purchase.getPhone();
        this.postalCode = purchase.getPostalCode();
        this.address = purchase.getAddress();
        this.courseId = purchase.getCourseId();
        this.orderCode = purchase.getOrder();
        this.email = purchase.getEmail();
        this.name = purchase.getName();
        this.surname = purchase.getSurname();
        this.nifCif = purchase.getNifCif();
        this.company = purchase.getCompany();
        this.isCompany = purchase.isCompany();
        this.city = purchase.getCity();
        this.region = purchase.getRegion();
        this.country = purchase.getCountry();
        this.finantialStepOvercome = purchase.isProcessedInFinantialState();
        this.learningStepOvercome = purchase.isProcessedInLearningState();
    }

    public DBPurchase() {

    }

    public Purchase toPurchase() {
        var purchase = new Purchase(transactionId,
                orderCode,
                courseId,
                concept,
                description,
                price.doubleValue(),
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
                learningStepOvercome);
        purchase.setId(id);
        return purchase;
    }

    public int getId() {
        return id;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setFinantialStepOvercome(boolean value) {
        finantialStepOvercome = value;
    }

    public void setLearningStepOvercome(boolean value) {
        learningStepOvercome = value;
    }
}
