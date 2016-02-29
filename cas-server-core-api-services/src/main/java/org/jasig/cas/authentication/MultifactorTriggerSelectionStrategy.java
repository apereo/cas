package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;

/**
 * This interface is responsible for deciding which Multifactor provider to use based on the request, service, and
 * principal.
 *
 * @author Daniel Frett
 * @since 4.3.0
 */
public interface MultifactorTriggerSelectionStrategy {
    /**
     * Resolve the multifactor authentication provider id for the specified HttpServletRequest, RegisteredService and
     * Principal.
     *
     * @param availableProviders a Set of available MFA providers
     * @param request            The original request to check for MFA requirements
     * @param service            The service to check for MFA requirements
     * @param principal          The principal to check for MFA requirements
     * @return the id of the MFA provider required for authentication
     */
    Optional<String> resolve(Set<String> availableProviders, HttpServletRequest request, RegisteredService service,
                             Principal principal);
}
