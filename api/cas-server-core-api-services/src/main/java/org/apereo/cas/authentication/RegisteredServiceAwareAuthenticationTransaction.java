package org.apereo.cas.authentication;

import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link RegisteredServiceAwareAuthenticationTransaction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface RegisteredServiceAwareAuthenticationTransaction extends AuthenticationTransaction {
    /**
     * Gets registered service.
     *
     * @return the registered service
     */
    RegisteredService getRegisteredService();
}
