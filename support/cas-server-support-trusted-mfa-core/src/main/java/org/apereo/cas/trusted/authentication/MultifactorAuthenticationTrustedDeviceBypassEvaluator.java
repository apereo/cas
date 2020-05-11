package org.apereo.cas.trusted.authentication;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link MultifactorAuthenticationTrustedDeviceBypassEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationTrustedDeviceBypassEvaluator {

    /**
     * Should bypass trusted device boolean.
     *
     * @param registeredService the registered service
     * @param service           the service
     * @param authentication    the authentication
     * @return true/false
     */
    boolean shouldBypassTrustedDevice(RegisteredService registeredService,
                                      Service service,
                                      Authentication authentication);
}
