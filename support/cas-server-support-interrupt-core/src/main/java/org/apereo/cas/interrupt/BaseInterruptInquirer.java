package org.apereo.cas.interrupt;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link BaseInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public abstract class BaseInterruptInquirer implements InterruptInquirer {
    @Override
    public final InterruptResponse inquire(final Authentication authentication, final RegisteredService registeredService, final Service service) {
        return inquireInternal(authentication, registeredService, service);
    }

    /**
     * Inquire internal interrupt response.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param service           the service
     * @return the interrupt response
     */
    protected abstract InterruptResponse inquireInternal(Authentication authentication, RegisteredService registeredService, Service service);
}
