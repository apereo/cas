package org.apereo.cas.validation;

import org.apereo.cas.authentication.principal.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link ServiceTicketValidationAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface ServiceTicketValidationAuthorizer {

    /**
     * Is authorized?
     *
     * @param request   the request
     * @param service   the service
     * @param assertion the assertion
     */
    void authorize(HttpServletRequest request, Service service, Assertion assertion);
}
