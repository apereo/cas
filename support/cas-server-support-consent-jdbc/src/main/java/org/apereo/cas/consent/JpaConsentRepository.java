package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link JpaConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerConsent")
public class JpaConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 6599902742493270206L;

    private static final String SELECT_QUERY = "SELECT r from ConsentDecision r ";

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaConsentRepository.class);

    @PersistenceContext(unitName = "consentEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions(final String principal) {
        try {
            return this.entityManager.createQuery(
                    SELECT_QUERY.concat("where r.principal = :principal"), ConsentDecision.class)
                    .setParameter("principal", principal)
                    .getResultList();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return new ArrayList<>(0);
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions() {
        try {
            return this.entityManager.createQuery(SELECT_QUERY, ConsentDecision.class).getResultList();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return new ArrayList<>(0);
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        try {
            return this.entityManager.createQuery(
                    SELECT_QUERY.concat("where r.principal = :principal and r.service = :service"), ConsentDecision.class)
                    .setParameter("principal", authentication.getPrincipal().getId())
                    .setParameter("service", service.getId())
                    .getSingleResult();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        try {
            final boolean isNew = decision.getId() < 0;
            final ConsentDecision mergedDecision = this.entityManager.merge(decision);
            if (!isNew) {
                this.entityManager.persist(mergedDecision);
            }
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
    
    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        try {
            final ConsentDecision decision = this.entityManager.createQuery(SELECT_QUERY
                    .concat("where r.id = :id"), ConsentDecision.class)
                    .setParameter("id", decisionId)
                    .getSingleResult();
            this.entityManager.remove(decision);
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
