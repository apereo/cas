package org.apereo.cas.logout;

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
    void registerLogoutHandler(LogoutHandler handler);

    /**
     * Gets logout handlers.
     *
     * @return the logout handlers
     */
    Collection<LogoutHandler> getLogoutHandlers();
}
