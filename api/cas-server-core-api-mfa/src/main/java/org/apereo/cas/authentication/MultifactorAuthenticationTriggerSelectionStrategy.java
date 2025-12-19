package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This interface is responsible for deciding which Multifactor provider to use based on the request, service, and
 * principal.
 *
 * @author Daniel Frett
 * @since 5.0.0
 */
public interface MultifactorAuthenticationTriggerSelectionStrategy {
    /**
     * Default Bean name.
     */
    String BEAN_NAME = "defaultMultifactorTriggerSelectionStrategy";

    /**
     * Resolve the multifactor authentication provider id.
     *
     * @param request           The original request to check for MFA requirements
     * @param response          the response
     * @param registeredService The service to check for MFA requirements
     * @param authentication    The authentication to check for MFA requirements
     * @param service           the service
     * @return the provider id of the MFA provider required for authentication
     * @throws Throwable the throwable
     */
    Optional<MultifactorAuthenticationProvider> resolve(HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        @Nullable RegisteredService registeredService,
                                                        Authentication authentication,
                                                        @Nullable Service service) throws Throwable;

    /**
     * Gets multifactor authentication triggers.
     *
     * @return the multifactor authentication triggers
     */
    Collection<MultifactorAuthenticationTrigger> getMultifactorAuthenticationTriggers();
}
