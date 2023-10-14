package com.codurance.katalyst.payment.application.integration.database;

import com.codurance.katalyst.payment.application.fixtures.PaymentTransactionFixtures;
import com.codurance.katalyst.payment.application.fixtures.PurchaseFixtures;
import com.codurance.katalyst.payment.application.infrastructure.database.payment.DBPaymentTransaction;
import com.codurance.katalyst.payment.application.infrastructure.database.purchase.DBPurchase;
import com.codurance.katalyst.payment.application.infrastructure.database.purchase.DBPurchaseRepository;
import com.codurance.katalyst.payment.application.infrastructure.database.purchase.PurchaseRepositoryJPA;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@ComponentScan(basePackages = {"com.codurance.katalyst.payment.application.infrastructure.database.payment"})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PurchaseRepositoryShould {
    @Autowired
    public TestEntityManager entityManager;

    @Autowired
    public DBPurchaseRepository jpaRepository;

    public PurchaseRepositoryJPA purchaseRepository;
    private Purchase purchase;
    private PurchaseFixtures purchaseFixtures = new PurchaseFixtures();

    private PaymentTransactionFixtures paymentTransactionFixtures = new PaymentTransactionFixtures();
    private int transactionId;

    @Before
    public void beforeEach() {
        purchaseRepository = new PurchaseRepositoryJPA(jpaRepository);
        purchase = purchaseFixtures.createPurchase();
        transactionId = persistADefaultPaymentTransaction();
        purchase.setTransactionId(transactionId);
    }

    private int persistADefaultPaymentTransaction() {
        var paymentTransaction = paymentTransactionFixtures.createPaymentTransaction();
        var dbPaymentTransaction = new DBPaymentTransaction(paymentTransaction);
        dbPaymentTransaction = entityManager.persist(dbPaymentTransaction);
        return dbPaymentTransaction.getId();
    }

    @Test
    public void save_a_transaction_which_doesnt_exist() {
        var savedPurchase = purchaseRepository.save(purchase);
        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getId()).isNotEqualTo(0);
        purchaseFixtures.assertTheSameDataThan(savedPurchase, purchase);
    }

    @Test
    public void save_a_transaction_which_exist_in_database() {
        var dbEntity = new DBPurchase(purchase);
        dbEntity = entityManager.persist(dbEntity);
        var dbId = dbEntity.getId();
        purchase.setId(dbId);
        var newAddress = "NEW ADDRESS";
        purchase.setAddress(newAddress);

        var savedPurchase = purchaseRepository.save(purchase);

        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getId()).isEqualTo(dbId);
        assertThat(savedPurchase.getAddress()).isEqualTo(newAddress);
    }

    @Test
    public void find_a_purchase_by_transaction_id() {
        var dbEntity = new DBPurchase(purchase);
        dbEntity = entityManager.persist(dbEntity);
        var dbId = dbEntity.getId();
        var dbTransactionId = dbEntity.getTransactionId();

        var savedPurchase = purchaseRepository.findPurchaseByTransactionId(dbTransactionId);

        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getId()).isEqualTo(dbId);
        assertThat(savedPurchase.getTransactionId()).isEqualTo(dbTransactionId);
    }

    @Test
    public void find_a_purchase_by_id() {
        var dbEntity = new DBPurchase(purchase);
        dbEntity = entityManager.persist(dbEntity);
        var dbId = dbEntity.getId();

        var savedPurchase = purchaseRepository.findPurchaseById(dbId);

        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getId()).isEqualTo(dbId);
    }
}

