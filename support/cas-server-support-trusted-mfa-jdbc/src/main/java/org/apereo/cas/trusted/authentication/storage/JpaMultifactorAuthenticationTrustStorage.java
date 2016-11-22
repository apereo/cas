package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link JpaMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(readOnly = false, transactionManager = "transactionManagerMfaAuthnTrust")
public class JpaMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private static final String TABLE_NAME = "MultifactorAuthenticationTrustRecord";

    @PersistenceContext(unitName = "mfaTrustedAuthnEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public void expire(final String key) {
        try {
            final int count = this.entityManager.createQuery("DELETE FROM " + TABLE_NAME + " r where r.key = :key",
                    MultifactorAuthenticationTrustRecord.class)
                    .setParameter("key", key)
                    .executeUpdate();
            logger.info("Found and removed {} records", count);
        } catch (final NoResultException e) {
            logger.info("No trusted authentication records could be found");
        }
    }

    @Override
    public void expire(final LocalDate onOrBefore) {
        try {
            final int count = this.entityManager.createQuery("DELETE FROM " + TABLE_NAME + " r where r.date < :date",
                    MultifactorAuthenticationTrustRecord.class)
                    .setParameter("date", onOrBefore)
                    .executeUpdate();
            logger.info("Found and removed {} records", count);
        } catch (final NoResultException e) {
            logger.info("No trusted authentication records could be found");
        }
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDate onOrAfterDate) {
        try {
            return new HashSet<>(this.entityManager.createQuery("SELECT r FROM " + TABLE_NAME + " r where r.date >= :date",
                    MultifactorAuthenticationTrustRecord.class).setParameter("date", onOrAfterDate).getResultList());
        } catch (final NoResultException e) {
            logger.info("No trusted authentication records could be found for {}", onOrAfterDate);
        }
        return Collections.emptySet();
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        try {
            return new HashSet<>(this.entityManager.createQuery("SELECT r FROM " + TABLE_NAME + " r where r.principal = :principal",
                    MultifactorAuthenticationTrustRecord.class).setParameter("principal", principal).getResultList());
        } catch (final NoResultException e) {
            logger.info("No trusted authentication records could be found for {}", principal);
        }
        return Collections.emptySet();
    }

    @Override
    public MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        return this.entityManager.merge(record);
    }
}
