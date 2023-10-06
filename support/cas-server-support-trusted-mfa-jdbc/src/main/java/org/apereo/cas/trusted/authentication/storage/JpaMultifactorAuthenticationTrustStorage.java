package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.jpa.AbstractJpaEntityFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.storage.generic.JpaMultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.DateTimeUtils;
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
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @PersistenceContext(unitName = "jpaMfaTrustedAuthnContext")
    private EntityManager entityManager;

    public JpaMultifactorAuthenticationTrustStorage(final TrustedDevicesMultifactorProperties properties,
                                                    final CipherExecutor<Serializable, String> cipherExecutor,
                                                    final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy,
                                                    final TransactionOperations transactionTemplate) {
        super(properties, cipherExecutor, keyGenerationStrategy);
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void remove(final ZonedDateTime expirationDate) {
        transactionTemplate.executeWithoutResult(__ -> {
            val value = DateTimeUtils.dateOf(expirationDate);
            LOGGER.trace("Removing expired records on or after [{}]", value);
            val count = entityManager.createQuery("DELETE FROM " + ENTITY_NAME + " r WHERE :expirationDate >= r.expirationDate")
                .setParameter("expirationDate", value)
                .executeUpdate();
            LOGGER.info("Found and removed [{}] records", count);
        });
    }

    @Override
    public void remove() {
        transactionTemplate.executeWithoutResult(__ -> remove(ZonedDateTime.now(ZoneOffset.UTC)));
    }

    @Override
    public void remove(final String key) {
        transactionTemplate.executeWithoutResult(__ -> {
            val count = entityManager.createQuery("DELETE FROM " + ENTITY_NAME + " r WHERE r.recordKey = :key")
                .setParameter("key", key)
                .executeUpdate();
            LOGGER.info("Found and removed [{}] records", count);
        });
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        return transactionTemplate.execute(__ -> {
            remove();
            val query = entityManager.createQuery(QUERY_SELECT, getEntityFactory().getType());
            val results = query.getResultList();
            return new HashSet<>(results);
        });
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        return transactionTemplate.execute(__ -> {
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
        return transactionTemplate.execute(__ -> {
            remove();
            val query = entityManager
                .createQuery(QUERY_SELECT + " WHERE r.principal = :principal", getEntityFactory().getType())
                .setParameter("principal", principal);
            val results = query.getResultList();
            return new HashSet<>(results);
        });
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        try {
            return transactionTemplate.execute(__ -> {
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
        return FunctionUtils.doUnchecked(() -> {
            val destination = getEntityFactory().newInstance();
            BeanUtils.copyProperties(destination, record);
            LOGGER.trace("Saving multifactor authentication trust record [{}]", destination);
            return transactionTemplate.execute(__ -> entityManager.merge(destination));
        });
    }

    private AbstractJpaEntityFactory<MultifactorAuthenticationTrustRecord> getEntityFactory() {
        return new JpaMultifactorAuthenticationTrustRecordEntityFactory(
            getTrustedDevicesMultifactorProperties().getJpa().getDialect());
    }
}
