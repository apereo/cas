package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;

/**
 * Functional interface to provide a method to validate an authentication against a requested context.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
public interface RequestedAuthenticationContextValidator {

    /**
     * Inspect the current authentication to validate the current requested context.
     *
     * @param assertion - the assertion
     * @param request   - the request
     * @return - status
     */
    AuthenticationContextValidationResult validateAuthenticationContext(Assertion assertion, HttpServletRequest request);

    /**
     * Validate authentication context.
     *
     * @param request           the request
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @param service           the service
     * @return the authentication context validation result
     */
    AuthenticationContextValidationResult validateAuthenticationContext(HttpServletRequest request,
                                                                        RegisteredService registeredService,
                                                                        Authentication authentication,
                                                                        Service service);
}
