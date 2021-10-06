package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link ConsentActivationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface ConsentActivationStrategy {
    /**
     * Bean name.
     */
    String BEAN_NAME = "consentActivationStrategy";

    /**
     * Determine if consent is required.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @param request           the request
     * @return true/false
     */
    boolean isConsentRequired(Service service,
                              RegisteredService registeredService,
                              Authentication authentication,
                              HttpServletRequest request);
}
