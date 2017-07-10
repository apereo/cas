package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;

/**
 * This is {@link SingleLogoutServiceMessageHandler} which defines how a logout message
 * for a service that supports SLO should be handled.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface SingleLogoutServiceMessageHandler {

    /**
     * Handle logout for slo service.
     *
     * @param singleLogoutService the service
     * @param ticketId the ticket id
     * @return the logout request
     */
    LogoutRequest handle(WebApplicationService singleLogoutService, String ticketId);
}
