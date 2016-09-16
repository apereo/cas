package org.apereo.cas.trusted.authentication.storage;

import com.google.common.collect.Sets;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

/**
 * This is {@link JpaMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(readOnly = false, transactionManager = "transactionManagerMfaAuthnTrust")
public class JpaMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {

    @PersistenceContext(unitName = "mfaTrustedAuthnEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        try {
            final List<MultifactorAuthenticationTrustRecord> results =
                    this.entityManager.createQuery("SELECT r FROM MultifactorAuthenticationTrustRecord r where r.principal = :principal",
                            MultifactorAuthenticationTrustRecord.class).setParameter("principal", principal).getResultList();
            return Sets.newHashSet(results);
        } catch (final NoResultException e) {
            logger.info("No trusted authentication records could be found for {}", principal);
        }
        return Sets.newHashSet();
    }

    @Override
    public MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        return this.entityManager.merge(record);
    }
}
