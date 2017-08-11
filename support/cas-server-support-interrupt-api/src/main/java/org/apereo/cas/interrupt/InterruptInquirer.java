package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link InterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface InterruptInquirer {

    /**
     * Inquire interrupt response.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param service           the service
     * @return the interrupt response
     */
    InterruptResponse inquire(Authentication authentication, RegisteredService registeredService, Service service);
}
