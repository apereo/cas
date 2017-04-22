package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link ConsentEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface ConsentEngine extends Serializable {

    /**
     * Gets consentable attributes.
     *
     * @param authentication    the authentication
     * @param service           the service
     * @param registeredService the registered service
     * @return the consentable attributes
     */
    Map<String, Object> getConsentableAttributes(Authentication authentication,
                                                 Service service,
                                                 RegisteredService registeredService);

    /**
     * Is consent required?
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @return the boolean
     */
    boolean isConsentRequiredFor(Service service, RegisteredService registeredService, Authentication authentication);
}
