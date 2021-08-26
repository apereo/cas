package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link ConsentActivationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface ConsentActivationStrategy {

    /**
     * Determine if consent is required.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @param requestContext    the request context
     * @return the boolean
     */
    boolean isConsentRequired(Service service,
                              RegisteredService registeredService,
                              Authentication authentication,
                              RequestContext requestContext);
}
