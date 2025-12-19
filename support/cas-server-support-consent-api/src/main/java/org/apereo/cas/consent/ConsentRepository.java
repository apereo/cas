package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link ConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface ConsentRepository extends Serializable {
    /**
     * Bean name.
     */
    String BEAN_NAME = "consentRepository";

    /**
     * Gets consent decision.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @return the consent decision
     */
    ConsentDecision findConsentDecision(Service service,
                                        RegisteredService registeredService,
                                        Authentication authentication);

    /**
     * Gets consent decision for a user.
     *
     * @param principal the principal
     * @return the consent decision
     */
    Collection<? extends ConsentDecision> findConsentDecisions(String principal);

    /**
     * Gets consent decisions.
     *
     * @return the consent decision
     */
    Collection<? extends ConsentDecision> findConsentDecisions();

    /**
     * Store consent decision.
     *
     * @param decision the decision
     * @return consent decision updated/added
     * @throws Throwable the throwable
     */
    ConsentDecision storeConsentDecision(ConsentDecision decision) throws Throwable;

    /**
     * Delete consent decision.
     *
     * @param id        the decision id
     * @param principal the principal
     * @return true / false
     * @throws Throwable the throwable
     */
    boolean deleteConsentDecision(long id, String principal) throws Throwable;

    /**
     * Delete consent decisions.
     *
     * @param principal the principal
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean deleteConsentDecisions(String principal) throws Throwable;

    /**
     * Delete all.
     *
     * @throws Throwable the throwable
     */
    void deleteAll() throws Throwable;
}
