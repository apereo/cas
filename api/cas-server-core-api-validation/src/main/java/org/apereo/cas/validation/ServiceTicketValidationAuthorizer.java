package org.apereo.cas.validation;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link ServiceTicketValidationAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface ServiceTicketValidationAuthorizer {

    /**
     * Determines whether service ticket validation is authorized.
     *
     * @param request   the request
     * @param service   the service
     * @param assertion the assertion
     */
    void authorize(HttpServletRequest request, Service service, Assertion assertion);
}
