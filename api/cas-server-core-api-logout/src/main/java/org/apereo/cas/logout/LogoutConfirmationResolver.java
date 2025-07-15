package org.apereo.cas.logout;

import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link LogoutConfirmationResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@FunctionalInterface
public interface LogoutConfirmationResolver {
    /**
     * Default bean name.
     */
    String DEFAULT_BEAN_NAME = "logoutConfirmationResolver";

    /**
     * Check if logout request is required or acknowledged.
     *
     * @param requestContext the request context
     * @return true/false
     */
    boolean isLogoutRequestConfirmed(RequestContext requestContext);
}
