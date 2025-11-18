package io.github.jhipster.sample.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jhipster.sample.IntegrationTest;
import io.github.jhipster.sample.domain.BankAccount;
import io.github.jhipster.sample.domain.Operation;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link OperationRepository}.
 */
@IntegrationTest
@Transactional
class OperationRepositoryTest {

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private Operation operation;
    private BankAccount bankAccount;
    private Long numberOfOperations;

    @BeforeEach
    void countOperations() {
        numberOfOperations = operationRepository.count();
    }

    @BeforeEach
    void init() {
        bankAccount = new BankAccount();
        bankAccount.setName("Test Account");
        bankAccount.setBalance(new BigDecimal(1000));
        bankAccount = bankAccountRepository.saveAndFlush(bankAccount);

        operation = new Operation();
        operation.setDate(Instant.now());
        operation.setDescription("Test Operation");
        operation.setAmount(new BigDecimal(100));
        operation.setBankAccount(bankAccount);
    }

    @AfterEach
    void cleanup() {
        if (operation != null && operation.getId() != null) {
            operationRepository.deleteById(operation.getId());
        }
        if (bankAccount != null && bankAccount.getId() != null) {
            bankAccountRepository.deleteById(bankAccount.getId());
        }
        assertThat(operationRepository.count()).isEqualTo(numberOfOperations);
        numberOfOperations = null;
    }

    @Test
    @Transactional
    void assertThatFindOneWithEagerRelationshipsWorks() {
        operation = operationRepository.saveAndFlush(operation);

        Optional<Operation> foundOperation = operationRepository.findOneWithEagerRelationships(operation.getId());
        assertThat(foundOperation).isPresent();
        assertThat(foundOperation.orElseThrow().getBankAccount()).isNotNull();
    }

    @Test
    @Transactional
    void assertThatFindOneWithEagerRelationshipsReturnsEmptyForNonExistent() {
        Optional<Operation> foundOperation = operationRepository.findOneWithEagerRelationships(Long.MAX_VALUE);
        assertThat(foundOperation).isEmpty();
    }

    @Test
    @Transactional
    void assertThatFindAllWithEagerRelationshipsWorks() {
        operation = operationRepository.saveAndFlush(operation);

        List<Operation> operations = operationRepository.findAllWithEagerRelationships();
        assertThat(operations).isNotEmpty();
        assertThat(operations.stream().anyMatch(o -> o.getId().equals(operation.getId()))).isTrue();
        // Verify eager loading
        operations.forEach(op -> assertThat(op.getBankAccount()).isNotNull());
    }

    @Test
    @Transactional
    void assertThatFindAllWithEagerRelationshipsWithPaginationWorks() {
        operation = operationRepository.saveAndFlush(operation);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Operation> operations = operationRepository.findAllWithEagerRelationships(pageable);
        assertThat(operations.getContent()).isNotEmpty();
        assertThat(operations.getContent().stream().anyMatch(o -> o.getId().equals(operation.getId()))).isTrue();
        // Verify eager loading
        operations.getContent().forEach(op -> assertThat(op.getBankAccount()).isNotNull());
    }

    @Test
    @Transactional
    void assertThatFindOneWithToOneRelationshipsWorks() {
        operation = operationRepository.saveAndFlush(operation);

        Optional<Operation> foundOperation = operationRepository.findOneWithToOneRelationships(operation.getId());
        assertThat(foundOperation).isPresent();
        assertThat(foundOperation.orElseThrow().getBankAccount()).isNotNull();
    }

    @Test
    @Transactional
    void assertThatFindAllWithToOneRelationshipsWorks() {
        operation = operationRepository.saveAndFlush(operation);

        List<Operation> operations = operationRepository.findAllWithToOneRelationships();
        assertThat(operations).isNotEmpty();
        assertThat(operations.stream().anyMatch(o -> o.getId().equals(operation.getId()))).isTrue();
        operations.forEach(op -> assertThat(op.getBankAccount()).isNotNull());
    }

    @Test
    @Transactional
    void assertThatFindAllWithToOneRelationshipsWithPaginationWorks() {
        operation = operationRepository.saveAndFlush(operation);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Operation> operations = operationRepository.findAllWithToOneRelationships(pageable);
        assertThat(operations.getContent()).isNotEmpty();
        assertThat(operations.getContent().stream().anyMatch(o -> o.getId().equals(operation.getId()))).isTrue();
        operations.getContent().forEach(op -> assertThat(op.getBankAccount()).isNotNull());
    }
}
