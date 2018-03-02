package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link JpaMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerMfaAuthnTrust")
public class JpaMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaMultifactorAuthenticationTrustStorage.class);
    
    private static final String TABLE_NAME = "MultifactorAuthenticationTrustRecord";

    @PersistenceContext(unitName = "mfaTrustedAuthnEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public void expire(final String key) {
        try {
            final int count = this.entityManager.createQuery("DELETE FROM " + TABLE_NAME + " r where r.recordKey = :key",
                    MultifactorAuthenticationTrustRecord.class)
                    .setParameter("key", key)
                    .executeUpdate();
            LOGGER.info("Found and removed [{}] records", count);
        } catch (final NoResultException e) {
            LOGGER.info("No trusted authentication records could be found");
        }
    }

    @Override
    public void expire(final LocalDate onOrBefore) {
        try {
            final int count = this.entityManager.createQuery("DELETE FROM " + TABLE_NAME + " r where r.recordDate < :date",
                    MultifactorAuthenticationTrustRecord.class)
                    .setParameter("date", onOrBefore)
                    .executeUpdate();
            LOGGER.info("Found and removed [{}] records", count);
        } catch (final NoResultException e) {
            LOGGER.info("No trusted authentication records could be found");
        }
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDate onOrAfterDate) {
        try {
            final List<MultifactorAuthenticationTrustRecord> results =
                    this.entityManager.createQuery("SELECT r FROM " + TABLE_NAME + " r where r.date >= :date",
                            MultifactorAuthenticationTrustRecord.class).setParameter("date", onOrAfterDate).getResultList();
            return new HashSet<>(results);
        } catch (final NoResultException e) {
            LOGGER.info("No trusted authentication records could be found for [{}]", onOrAfterDate);
        }
        return new HashSet<>(0);
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        try {
            final List<MultifactorAuthenticationTrustRecord> results =
                    this.entityManager.createQuery("SELECT r FROM " + TABLE_NAME + " r where r.principal = :principal",
                            MultifactorAuthenticationTrustRecord.class).setParameter("principal", principal).getResultList();
            return new HashSet<>(results);
        } catch (final NoResultException e) {
            LOGGER.info("No trusted authentication records could be found for [{}]", principal);
        }
        return new HashSet<>(0);
    }

    @Override
    public MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        return this.entityManager.merge(record);
    }
}
