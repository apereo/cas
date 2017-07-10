package org.apereo.cas.authentication;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;

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
@FunctionalInterface
public interface MultifactorTriggerSelectionStrategy {
    /**
     * Resolve the multifactor authentication provider id for the specified HttpServletRequest, RegisteredService and
     * Principal.
     *
     * @param providers a Map of available MFA providers loaded in the spring context
     * @param request   The original request to check for MFA requirements
     * @param service   The service to check for MFA requirements
     * @param principal The principal to check for MFA requirements
     * @return the provider id of the MFA provider required for authentication
     */
    Optional<String> resolve(Collection<MultifactorAuthenticationProvider> providers, HttpServletRequest request, 
                             RegisteredService service,
                             Principal principal);
}
