package com.codurance.katalyst.payment.application.integration;

import com.codurance.katalyst.payment.application.infrastructure.database.payment.DBPaymentTransaction;
import com.codurance.katalyst.payment.application.infrastructure.database.payment.DBPaymentTransactionRepository;
import com.codurance.katalyst.payment.application.infrastructure.database.payment.TransactionRepositoryJPA;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentMethod;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransactionState;
import com.codurance.katalyst.payment.application.model.payment.entity.TransactionType;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
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

    public TransactionRepositoryJPA userRepository;
    private PaymentTransaction paymentTransaction;

    @Before
    public void beforeEach() {
        userRepository = new TransactionRepositoryJPA(jpaRepository);
        paymentTransaction = createPaymentTransactionFixture();
    }

    @Test
    public void save_a_transaction_which_doesnt_exist() {
        var savedPaymentTransaction = userRepository.save(paymentTransaction);
        assertThat(savedPaymentTransaction).isNotNull();
        assertThat(paymentTransaction.getId()).isNotEqualTo(0);
        assertHasSameData(paymentTransaction, savedPaymentTransaction);
    }

    @Test
    public void save_a_transaction_which_exist_in_database() {
        var paymentTransaction = createPaymentTransactionFixture();
        var dbEntity = new DBPaymentTransaction(paymentTransaction);
        dbEntity = entityManager.persist(dbEntity);
        var dbId = dbEntity.getId();
        paymentTransaction.setId(dbId);
        var newIp = "127.0.2.2";
        paymentTransaction.setIp(newIp);

        var savedPaymentTransaction = userRepository.save(paymentTransaction);

        assertThat(savedPaymentTransaction).isNotNull();
        assertThat(paymentTransaction.getId()).isEqualTo(dbId);
        assertThat(paymentTransaction.getIp()).isEqualTo(newIp);
    }

    @Test
    public void read_a_transaction_which_exist_in_database() {
        var paymentTransaction = createPaymentTransactionFixture();
        var paymentStatus = paymentTransaction.getPaymentStatus();
        var dbEntity = new DBPaymentTransaction(paymentTransaction);
        dbEntity = entityManager.persist(dbEntity);

        var savedPaymentTransaction = userRepository.getOpenTransactionBasedOn(paymentTransaction.getOrder());

        assertHasSameData(paymentTransaction, savedPaymentTransaction);
    }

    private void assertHasSameData(PaymentTransaction paymentTransaction,  PaymentTransaction savedPaymentTransaction) {
        PaymentStatus paymentStatus = paymentTransaction.getPaymentStatus();
        assertThat(savedPaymentTransaction).isNotNull();
        assertThat(savedPaymentTransaction.getIp()).isEqualTo(paymentTransaction.getIp());
        assertThat(savedPaymentTransaction.getPaymentMethod()).isEqualTo(paymentTransaction.getPaymentMethod());
        assertThat(savedPaymentTransaction.getTransactionType()).isEqualTo(paymentTransaction.getTransactionType());
        assertThat(savedPaymentTransaction.getTpvToken()).isEqualTo(paymentTransaction.getTpvToken());
        assertThat(savedPaymentTransaction.getTpvUser()).isEqualTo(paymentTransaction.getTpvUser());
        assertThat(savedPaymentTransaction.getOrder()).isEqualTo(paymentTransaction.getOrder());
        assertThat(savedPaymentTransaction.getAmount()).isEqualTo(paymentTransaction.getAmount());
        assertThat(savedPaymentTransaction.getDate()).isEqualTo(paymentTransaction.getDate());
        assertThat(savedPaymentTransaction.getTransactionState()).isEqualTo(paymentTransaction.getTransactionState());
        var savedPaymentStatus = savedPaymentTransaction.getPaymentStatus();
        assertThat(savedPaymentStatus.getOrder()).isEqualTo(paymentStatus.getOrder());
        assertThat(savedPaymentStatus.getAmount()).isEqualTo(paymentStatus.getAmount());
        assertThat(savedPaymentStatus.getChallengeUrl()).isEqualTo(paymentStatus.getChallengeUrl());
        assertThat(savedPaymentStatus.getCurrency()).isEqualTo(paymentStatus.getCurrency());
        assertThat(savedPaymentStatus.getErrorCode()).isEqualTo(paymentStatus.getErrorCode());
    }

    private PaymentTransaction createPaymentTransactionFixture() {
        var paymentStatus = new PaymentStatus();
        paymentStatus.setErrorCode(1);
        paymentStatus.setAmount(3456);
        paymentStatus.setCurrency("EUR");
        paymentStatus.setOrder("ORDER");
        paymentStatus.setChallengeUrl("CHALLENGE_URL");
        var paymentTransaction = new PaymentTransaction(
                "127.0.1.1",
                PaymentMethod.CARDS,
                TransactionType.AUTHORIZATION,
                "RANDOM_TPV_TOKEN",
                1,
                "RANDOM_ORDER_NAME",
                34.56,
                "20231205103259",
                PaymentTransactionState.PENDING,
                paymentStatus
        );
        return paymentTransaction;
    }
}
