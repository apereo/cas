package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import java.io.Serializable;
import java.util.Collection;

/**
 * This is {@link ConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface ConsentRepository extends Serializable {

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
     */
    ConsentDecision storeConsentDecision(ConsentDecision decision);

    /**
     * Delete consent decision.
     *
     * @param id        the decision id
     * @param principal the principal
     * @return true / false
     */
    boolean deleteConsentDecision(long id, String principal);

    /**
     * Delete consent decisions.
     *
     * @param principal the principal
     * @return the boolean
     */
    boolean deleteConsentDecisions(String principal);
}
