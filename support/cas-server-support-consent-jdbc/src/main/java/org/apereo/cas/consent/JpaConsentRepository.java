package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serial;
import java.util.Collection;

/**
 * This is {@link JpaConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = "transactionManagerConsent")
@Slf4j
@ToString
public class JpaConsentRepository implements ConsentRepository {

    @Serial
    private static final long serialVersionUID = 6599902742493270206L;

    private static final String ENTITY_NAME = "JpaConsentDecision";

    private static final String SELECT_QUERY = "SELECT r from " + ENTITY_NAME + " r ";

    @PersistenceContext(unitName = "jpaConsentContext")
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
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        return this.entityManager.createQuery(SELECT_QUERY.concat("WHERE r.principal = :principal"),
            JpaConsentDecision.class).setParameter("principal", principal).getResultList();
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        return this.entityManager.createQuery(SELECT_QUERY, JpaConsentDecision.class).getResultList();
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) {
        val account = ObjectUtils.getIfNull(
            this.entityManager.find(JpaConsentDecision.class, decision.getId()), new JpaConsentDecision());
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
        return mergedDecision;
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        try {
            val query = SELECT_QUERY.concat("WHERE r.principal = :principal AND r.id = :id");
            val decision = entityManager.createQuery(query, JpaConsentDecision.class)
                .setParameter("id", decisionId)
                .setParameter("principal", principal)
                .getSingleResult();
            entityManager.remove(decision);
            return true;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) {
        try {
            val query = SELECT_QUERY.concat("WHERE r.principal = :principal");
            val decision = entityManager.createQuery(query, JpaConsentDecision.class)
                .setParameter("principal", principal)
                .getSingleResult();
            entityManager.remove(decision);
            return true;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public void deleteAll() {
        val query = "DELETE FROM " + ENTITY_NAME;
        entityManager.createQuery(query).executeUpdate();
    }
}
