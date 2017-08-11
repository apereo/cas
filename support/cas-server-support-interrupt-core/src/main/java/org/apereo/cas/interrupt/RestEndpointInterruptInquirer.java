package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link RestEndpointInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestEndpointInterruptInquirer implements InterruptInquirer {
    @Override
    public InterruptResponse inquire(final Authentication authentication, final RegisteredService registeredService, final Service service) {
        return null;
    }
}
