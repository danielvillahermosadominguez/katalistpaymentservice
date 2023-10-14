package com.codurance.katalyst.payment.application.integration.database;

import com.codurance.katalyst.payment.application.fixtures.PaymentTransactionFixtures;
import com.codurance.katalyst.payment.application.infrastructure.database.payment.DBPaymentTransaction;
import com.codurance.katalyst.payment.application.infrastructure.database.payment.DBPaymentTransactionRepository;
import com.codurance.katalyst.payment.application.infrastructure.database.payment.TransactionRepositoryJPA;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
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
public class TransactionRepositoryShould {
    @Autowired
    public TestEntityManager entityManager;

    @Autowired
    public DBPaymentTransactionRepository jpaRepository;

    public TransactionRepositoryJPA paymentTransactionRepository;
    private PaymentTransaction paymentTransaction;

    private PaymentTransactionFixtures paymentTransactionFixture;

    @Before
    public void beforeEach() {
        paymentTransactionRepository = new TransactionRepositoryJPA(jpaRepository);
        paymentTransactionFixture = new PaymentTransactionFixtures();
        paymentTransaction = paymentTransactionFixture.createPaymentTransaction();
    }

    @Test
    public void save_a_transaction_which_doesnt_exist() {
        var savedPaymentTransaction = paymentTransactionRepository.save(paymentTransaction);
        assertThat(savedPaymentTransaction).isNotNull();
        assertThat(savedPaymentTransaction.getId()).isNotEqualTo(0);
        paymentTransactionFixture.assertHasSameData(paymentTransaction, savedPaymentTransaction);
    }

    @Test
    public void save_a_transaction_which_exist_in_database() {
        var dbEntity = new DBPaymentTransaction(paymentTransaction);
        dbEntity = entityManager.persist(dbEntity);
        var dbId = dbEntity.getId();
        paymentTransaction.setId(dbId);
        var newIp = "127.0.2.2";
        paymentTransaction.setIp(newIp);

        var savedPaymentTransaction = paymentTransactionRepository.save(paymentTransaction);

        assertThat(savedPaymentTransaction).isNotNull();
        assertThat(paymentTransaction.getId()).isEqualTo(dbId);
        assertThat(paymentTransaction.getIp()).isEqualTo(newIp);
    }

    @Test
    public void read_a_pending_transaction_which_exist_in_database() {
        paymentTransaction.setTransactionState(PaymentTransactionState.PENDING);
        var dbEntity = new DBPaymentTransaction(paymentTransaction);
        entityManager.persist(dbEntity);

        var savedPaymentTransaction = paymentTransactionRepository.getPendingPaymentTransactionBasedOn(paymentTransaction.getOrder());

        paymentTransactionFixture.assertHasSameData(savedPaymentTransaction, paymentTransaction);
    }

    @Test
    public void read_null_when_there_is_not_a_pending_transaction_with_this_order_code() {
        var savedPaymentTransaction = paymentTransactionRepository.getPendingPaymentTransactionBasedOn(paymentTransaction.getOrder());

        assertThat(savedPaymentTransaction).isNull();
    }

    @Test
    public void read_null_when_the_transaction_exists_but_is_done() {
        paymentTransaction.setTransactionState(PaymentTransactionState.DONE);
        var dbEntity = new DBPaymentTransaction(paymentTransaction);
        entityManager.persist(dbEntity);

        var savedPaymentTransaction = paymentTransactionRepository.getPendingPaymentTransactionBasedOn(paymentTransaction.getOrder());

        assertThat(savedPaymentTransaction).isNull();
    }
}
