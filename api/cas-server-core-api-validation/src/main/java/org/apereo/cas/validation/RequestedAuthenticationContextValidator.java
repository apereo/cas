package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
     * @param assertion          - the assertion
     * @param request            - the request
     * @param response           the response
     * @return - status
     * @throws Throwable the throwable
     */
    AuthenticationContextValidationResult validateAuthenticationContext(Assertion assertion, HttpServletRequest request,
                                                                        HttpServletResponse response) throws Throwable;

    /**
     * Validate authentication context.
     *
     * @param request            the request
     * @param response           the response
     * @param registeredService  the registered service
     * @param authentication     the authentication
     * @param service            the service
     * @return the authentication context validation result
     * @throws Throwable the throwable
     */
    AuthenticationContextValidationResult validateAuthenticationContext(HttpServletRequest request,
                                                                        HttpServletResponse response,
                                                                        RegisteredService registeredService,
                                                                        Authentication authentication,
                                                                        Service service) throws Throwable;
}
