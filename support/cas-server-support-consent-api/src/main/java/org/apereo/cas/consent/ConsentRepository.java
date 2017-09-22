package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import java.io.Serializable;

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
    ConsentDecision findConsentDecision(Service service, RegisteredService registeredService, Authentication authentication);
}
