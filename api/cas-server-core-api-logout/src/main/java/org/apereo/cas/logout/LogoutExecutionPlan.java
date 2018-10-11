package org.apereo.cas.logout;

import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link LogoutExecutionPlan} that describes how various CAS modules
 * must respond to the CAS logout events. A simple example of this may be OAuth
 * or OpenID Connect where Access Tokens and Refresh Tokens may need to be cleaned up
 * once the associated TGT is perhaps removed.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface LogoutExecutionPlan {

    /**
     * Register logout handler.
     *
     * @param handler the handler
     */
    default void registerLogoutPostProcessor(final LogoutPostProcessor handler) {
    }

    /**
     * Register single logout service message handler.
     *
     * @param handler the handler
     */
    default void registerSingleLogoutServiceMessageHandler(final SingleLogoutServiceMessageHandler handler) {
    }

    /**
     * Gets logout handlers.
     *
     * @return the logout handlers
     */
    default Collection<LogoutPostProcessor> getLogoutPostProcessor() {
        return new ArrayList<>(0);
    }

    /**
     * Gets single logout service message handlers.
     *
     * @return the single logout service message handlers
     */
    default Collection<SingleLogoutServiceMessageHandler> getSingleLogoutServiceMessageHandlers() {
        return new ArrayList<>(0);
    }
}
