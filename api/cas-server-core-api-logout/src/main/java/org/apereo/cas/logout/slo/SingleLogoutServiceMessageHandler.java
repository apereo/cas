package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;

import org.springframework.core.Ordered;

import java.util.Collection;

/**
 * This is {@link SingleLogoutServiceMessageHandler} which defines how a logout message
 * for a service that supports SLO should be handled.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface SingleLogoutServiceMessageHandler extends Ordered {

    /**
     * Handle logout for slo service.
     *
     * @param singleLogoutService the service
     * @param sessionIdentifier            the ticket id
     * @param context             the ticket granting ticket
     * @return the logout request
     */
    Collection<SingleLogoutRequestContext> handle(WebApplicationService singleLogoutService,
                                                  String sessionIdentifier, SingleLogoutExecutionRequest context);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Supports handling the logout message.
     *
     * @param context the context
     * @param service the service
     * @return true /false
     */
    default boolean supports(final SingleLogoutExecutionRequest context, final WebApplicationService service) {
        return service != null;
    }

    /**
     * Log out of a service through back channel.
     *
     * @param request the logout request.
     * @return if the logout has been performed.
     */
    boolean performBackChannelLogout(SingleLogoutRequestContext request);

    /**
     * Create a logout message typically for front channel logout.
     *
     * @param logoutRequest the logout request.
     * @return the single logout message payload
     */
    SingleLogoutMessage createSingleLogoutMessage(SingleLogoutRequestContext logoutRequest);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
