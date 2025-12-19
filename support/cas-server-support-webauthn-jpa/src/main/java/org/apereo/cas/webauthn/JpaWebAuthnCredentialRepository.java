package org.apereo.cas.webauthn;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.data.CredentialRegistration;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.transaction.support.TransactionOperations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * This is {@link JpaWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class JpaWebAuthnCredentialRepository extends BaseWebAuthnCredentialRepository {
    private static final String UPDATE_QUERY = String.format("UPDATE %s r ", JpaWebAuthnCredentialRegistration.ENTITY_NAME);

    private static final String SELECT_QUERY = String.format("SELECT r from %s r ", JpaWebAuthnCredentialRegistration.ENTITY_NAME);

    private final TransactionOperations transactionTemplate;

    @PersistenceContext(unitName = "jpaWebAuthnRegistryContext")
    private EntityManager entityManager;

    public JpaWebAuthnCredentialRepository(
        final CasConfigurationProperties properties,
        final CipherExecutor<String, String> cipherExecutor,
        final TransactionOperations transactionTemplate) {
        super(properties, cipherExecutor);
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        return transactionTemplate.execute(status -> {
            val records = entityManager.createQuery(
                    SELECT_QUERY.concat("WHERE r.username = :username"), JpaWebAuthnCredentialRegistration.class)
                .setParameter("username", username.trim().toLowerCase(Locale.ENGLISH))
                .getResultList();

            return records.stream()
                .map(record -> getCipherExecutor().decode(record.getRecords()))
                .map(Unchecked.function(record -> WebAuthnUtils.getObjectMapper().readValue(record, new TypeReference<Set<CredentialRegistration>>() {
                })))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        });
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
    public void update(final String username, final Collection<CredentialRegistration> givenRecords) {
        val records = givenRecords.stream()
            .map(record -> {
                if (record.getRegistrationTime() == null) {
                    return record.withRegistrationTime(Instant.now(Clock.systemUTC()));
                }
                return record;
            })
            .collect(Collectors.toList());
        val jsonRecords = FunctionUtils.doUnchecked(() -> getCipherExecutor().encode(WebAuthnUtils.getObjectMapper().writeValueAsString(records)));
        transactionTemplate.executeWithoutResult(status -> {
            val count = entityManager.createQuery(UPDATE_QUERY.concat("SET r.records=:records WHERE r.username = :username"))
                .setParameter("username", username.trim().toLowerCase(Locale.ENGLISH))
                .setParameter("records", jsonRecords)
                .executeUpdate();

            if (count == 0) {
                val record = JpaWebAuthnCredentialRegistration.builder()
                    .username(username.trim().toLowerCase(Locale.ENGLISH))
                    .records(jsonRecords)
                    .build();
                entityManager.merge(record);
            }
        });
    }
}
