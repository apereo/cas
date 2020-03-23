package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
@Slf4j
@ToString
public class JpaConsentRepository implements ConsentRepository {

    private static final long serialVersionUID = 6599902742493270206L;

    private static final String SELECT_QUERY = "SELECT r from JpaConsentDecision r ";

    @PersistenceContext(unitName = "consentEntityManagerFactory")
    private transient EntityManager entityManager;

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        try {
            val query = SELECT_QUERY.concat("WHERE r.principal = :principal AND r.service = :service");
            return this.entityManager.createQuery(query, JpaConsentDecision.class)
                .setParameter("principal", authentication.getPrincipal().getId())
                .setParameter("service", service.getId()).getSingleResult();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        try {
            return this.entityManager.createQuery(SELECT_QUERY.concat("where r.principal = :principal"),
                JpaConsentDecision.class).setParameter("principal", principal).getResultList();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return new ArrayList<>(0);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        try {
            return this.entityManager.createQuery(SELECT_QUERY, JpaConsentDecision.class).getResultList();
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return new ArrayList<>(0);
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        try {
            val account = new JpaConsentDecision();
            account.setId(decision.getId());
            account.setAttributes(decision.getAttributes());
            account.setCreatedDate(decision.getCreatedDate());
            account.setOptions(decision.getOptions());
            account.setPrincipal(decision.getPrincipal());
            account.setReminder(decision.getReminder());
            account.setReminderTimeUnit(decision.getReminderTimeUnit());
            account.setService(decision.getService());

            val isNew = account.getId() < 0;
            val mergedDecision = this.entityManager.merge(account);
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
            val decision = this.entityManager.createQuery(SELECT_QUERY.concat("WHERE r.id = :id"), JpaConsentDecision.class)
                .setParameter("id", decisionId).getSingleResult();
            this.entityManager.remove(decision);
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
