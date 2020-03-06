package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.Optional;

/**
 * This interface is responsible for deciding which Multifactor provider to use based on the request, service, and
 * principal.
 *
 * @author Daniel Frett
 * @since 5.0.0
 */
public interface MultifactorAuthenticationTriggerSelectionStrategy {

    /**
     * Resolve the multifactor authentication provider id.
     *
     * @param request           The original request to check for MFA requirements
     * @param registeredService The service to check for MFA requirements
     * @param authentication    The authentication to check for MFA requirements
     * @param service           the service
     * @return the provider id of the MFA provider required for authentication
     */
    Optional<String> resolve(HttpServletRequest request,
                             RegisteredService registeredService,
                             Authentication authentication,
                             Service service);

    /**
     * Gets multifactor authentication triggers.
     *
     * @return the multifactor authentication triggers
     */
    Collection<MultifactorAuthenticationTrigger> getMultifactorAuthenticationTriggers();
}
