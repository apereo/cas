package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;

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
     * @return the consent query result
     * @throws Throwable the throwable
     */
    ConsentQueryResult isConsentRequired(Service service,
                              RegisteredService registeredService,
                              Authentication authentication,
                              @Nullable HttpServletRequest request) throws Throwable;
}
