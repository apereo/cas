package org.apereo.cas.webauthn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.data.CredentialRegistration;
import lombok.SneakyThrows;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link JpaWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableTransactionManagement
@Transactional(transactionManager = "transactionManagerWebAuthn", propagation = Propagation.REQUIRED)
public class JpaWebAuthnCredentialRepository extends BaseWebAuthnCredentialRepository {
    private static final String UPDATE_QUERY = String.format("UPDATE %s r ", JpaWebAuthnCredentialRegistration.ENTITY_NAME);

    private static final String SELECT_QUERY = String.format("SELECT r from %s r ", JpaWebAuthnCredentialRegistration.ENTITY_NAME);

    private final PlatformTransactionManager transactionManager;

    @PersistenceContext(unitName = "webAuthnEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaWebAuthnCredentialRepository(
        final CasConfigurationProperties properties,
        final CipherExecutor<String, String> cipherExecutor,
        final PlatformTransactionManager transactionManager) {
        super(properties, cipherExecutor);
        this.transactionManager = transactionManager;
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        val records = entityManager.createQuery(
            SELECT_QUERY.concat("WHERE r.username = :username"), JpaWebAuthnCredentialRegistration.class)
            .setParameter("username", username.trim().toLowerCase())
            .getResultList();

        return records.stream()
            .map(record -> getCipherExecutor().decode(record.getRecords()))
            .map(Unchecked.function(record -> WebAuthnUtils.getObjectMapper().readValue(record, new TypeReference<Set<CredentialRegistration>>() {
            })))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    public Stream<CredentialRegistration> stream() {
        val records = entityManager.createQuery(SELECT_QUERY, JpaWebAuthnCredentialRegistration.class).getResultList();
        return records.stream()
            .map(record -> getCipherExecutor().decode(record.getRecords()))
            .map(Unchecked.function(record -> WebAuthnUtils.getObjectMapper().readValue(record, new TypeReference<Set<CredentialRegistration>>() {
            })))
            .flatMap(Collection::stream);
    }

    @Override
    @SneakyThrows
    public void update(final String username, final Collection<CredentialRegistration> givenRecords) {
        val records = givenRecords.stream()
            .map(record -> {
                if (record.getRegistrationTime() == null) {
                    return record.withRegistrationTime(Instant.now(Clock.systemUTC()));
                }
                return record;
            })
            .collect(Collectors.toList());
        val jsonRecords = getCipherExecutor().encode(WebAuthnUtils.getObjectMapper().writeValueAsString(records));
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                val count = entityManager.createQuery(UPDATE_QUERY.concat("SET r.records=:records WHERE r.username = :username"))
                    .setParameter("username", username.trim().toLowerCase())
                    .setParameter("records", jsonRecords)
                    .executeUpdate();

                if (count == 0) {
                    val record = JpaWebAuthnCredentialRegistration.builder()
                        .username(username.trim().toLowerCase())
                        .records(jsonRecords)
                        .build();
                    entityManager.merge(record);
                }
            }
        });
    }
}
