package org.apereo.cas.trusted.authentication.storage;

import module java.base;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.jpa.AbstractJpaEntityFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.storage.generic.JpaMultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import module java.sql;

/**
 * This is {@link JpaMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = "transactionManagerMfaAuthnTrust")
@Slf4j
public class JpaMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private static final String ENTITY_NAME = JpaMultifactorAuthenticationTrustRecord.class.getSimpleName();
    private static final String QUERY_SELECT = "SELECT r FROM " + ENTITY_NAME + " r ";

    private final TransactionOperations transactionTemplate;
    private final DataSource entityDataSource;

    @PersistenceContext(unitName = "jpaMfaTrustedAuthnContext")
    private EntityManager entityManager;

    public JpaMultifactorAuthenticationTrustStorage(final TrustedDevicesMultifactorProperties properties,
                                                    final CipherExecutor<Serializable, String> cipherExecutor,
                                                    final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy,
                                                    final TransactionOperations transactionTemplate,
                                                    final DataSource entityDataSource) {
        super(properties, cipherExecutor, keyGenerationStrategy);
        this.transactionTemplate = transactionTemplate;
        this.entityDataSource = entityDataSource;
    }

    @Override
    public void remove(final ZonedDateTime expirationDate) {
        transactionTemplate.executeWithoutResult(_ -> {
            val value = DateTimeUtils.dateOf(expirationDate);
            LOGGER.trace("Removing expired records on or after [{}]", value);
            val count = entityManager.createQuery("DELETE FROM " + ENTITY_NAME + " r WHERE :expirationDate >= r.expirationDate")
                .setParameter("expirationDate", value)
                .executeUpdate();
            LOGGER.info("Found and removed [{}] trusted records", count);
        });
    }

    @Override
    public void remove() {
        transactionTemplate.executeWithoutResult(_ -> remove(ZonedDateTime.now(ZoneOffset.UTC)));
    }

    @Override
    public void remove(final String key) {
        transactionTemplate.executeWithoutResult(_ -> {
            val count = entityManager.createQuery("DELETE FROM " + ENTITY_NAME + " r WHERE r.recordKey = :key")
                .setParameter("key", key)
                .executeUpdate();
            LOGGER.info("Found and removed [{}] records", count);
        });
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        return FunctionUtils.doAndHandle(() -> transactionTemplate.execute(_ -> {
            remove();
            val query = entityManager.createQuery(QUERY_SELECT, getEntityFactory().getType());
            val results = query.getResultList();
            return new HashSet<>(results);
        }), e -> new HashSet<MultifactorAuthenticationTrustRecord>()).get();
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        return transactionTemplate.execute(_ -> {
            remove();
            val query = entityManager
                .createQuery(QUERY_SELECT + " WHERE r.recordDate >= :date", getEntityFactory().getType())
                .setParameter("date", onOrAfterDate);
            val results = query.getResultList();
            return new HashSet<>(results);
        });
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        return FunctionUtils.doAndHandle(() -> transactionTemplate.execute(_ -> {
            remove();
            val query = entityManager
                .createQuery(QUERY_SELECT + " WHERE r.principal = :principal", getEntityFactory().getType())
                .setParameter("principal", principal);
            val results = query.getResultList();
            return new HashSet<>(results);
        }), e -> new HashSet<MultifactorAuthenticationTrustRecord>()).get();
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        try {
            return transactionTemplate.execute(_ -> {
                remove();
                val query = entityManager
                    .createQuery(QUERY_SELECT + " WHERE r.id >= :id", getEntityFactory().getType())
                    .setParameter("id", id)
                    .setMaxResults(1);
                return query.getSingleResult();
            });
        } catch (final NoResultException e) {
            LOGGER.debug("No trusted authentication records could be found for [{}]", id);
        }
        return null;
    }

    @Override
    public MultifactorAuthenticationTrustRecord saveInternal(final MultifactorAuthenticationTrustRecord record) {
        return FunctionUtils.doAndHandle(() -> {
            val destination = getEntityFactory().newInstance();
            BeanUtils.copyProperties(destination, record);
            LOGGER.trace("Saving multifactor authentication trust record [{}]", destination);
            return transactionTemplate.execute(_ -> entityManager.merge(destination));
        }, e -> record).get();
    }


    @Override
    public boolean isAvailable() {
        try (val connection = entityDataSource.getConnection()) {
            return connection != null;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return false;
        }
    }

    private AbstractJpaEntityFactory<MultifactorAuthenticationTrustRecord> getEntityFactory() {
        return new JpaMultifactorAuthenticationTrustRecordEntityFactory(
            getTrustedDevicesMultifactorProperties().getJpa().getDialect());
    }
}
