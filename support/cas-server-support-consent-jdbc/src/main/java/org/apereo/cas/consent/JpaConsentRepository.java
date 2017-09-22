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

/**
 * This is {@link JpaConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerConsent")
public class JpaConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 6599908862493270206L;

    private static final String SELECT_QUERY = "SELECT r from ConsentDecision r ";

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaConsentRepository.class);

    @PersistenceContext(unitName = "consentEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public String toString() {
        return getClass().getSimpleName();
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
            return null;
        }
    }
}
